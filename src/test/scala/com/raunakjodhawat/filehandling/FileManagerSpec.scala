package com.raunakjodhawat.filehandling

import zio.test.Assertion.equalTo
import zio.test._
import zio.{Scope, ZIO}
import zio.test.TestAspect.{beforeAll, sequential}
import zio.test.{Spec, TestEnvironment}
import zio.test.junit.JUnitRunnableSpec

import java.io.File
import scala.reflect.runtime.universe
import scala.reflect.runtime.universe.TermName
import scala.util.{Success, Using}

object FileManagerSpec extends JUnitRunnableSpec {
  val fileLocation = "src/test/resources/file.txt"
  val nonExistentFileLocation = "src/test/resources/non-existent-file.txt"
  val fileManager = new FileManager(fileLocation)
  val nonExistentFileManager = new FileManager(nonExistentFileLocation)
  val beforeAllHook: ZIO[Any, Throwable, Unit] = ZIO.attempt {
    val file = new java.io.File(fileLocation)
    if (file.exists()) file.delete()
  }.unit
  def invokePrivateMethod[T](obj: AnyRef, methodName: String, args: Any*): T = {
    val mirror = universe.runtimeMirror(obj.getClass.getClassLoader)
    val instanceMirror = mirror.reflect(obj)
    val methodSymbol =
      instanceMirror.symbol.typeSignature.member(TermName(methodName)).asMethod
    val method = instanceMirror.reflectMethod(methodSymbol)
    method(args: _*).asInstanceOf[T]
  }
  override def spec: Spec[TestEnvironment with Scope, Throwable] =
    suite("File Manager Spec")(
      test("File does not exists") {
        invokePrivateMethod[ZIO[Any, Throwable, Boolean]](
          fileManager,
          "fileExists"
        ).flatMap(exists => ZIO.succeed(assert(exists)(equalTo(false))))
      },
      test("Create file") {
        invokePrivateMethod[ZIO[Any, Throwable, Boolean]](
          fileManager,
          "safeCreate"
        ) *> ZIO
          .attempt(new File(fileLocation))
          .flatMap(file => ZIO.succeed(assert(file.exists())(equalTo(true))))
      },
      test("Append to file") {
        val content = List("Hello", "World")
        fileManager.appendToFile(content) *> ZIO
          .attempt(Using(scala.io.Source.fromFile(fileLocation)) { source =>
            source.getLines().toList
          })
          .flatMap(mayBeFileContent =>
            mayBeFileContent match {
              case Success(fileContent) =>
                ZIO.succeed(assert(fileContent)(equalTo(content)))
              case _ => ZIO.fail(new Exception("File not found"))
            }
          )
      },
      test("update the file") {
        val oldContent = "Hello"
        val newContent = "Hi"
        fileManager.updateFile(oldContent, newContent) *> ZIO
          .attempt(Using(scala.io.Source.fromFile(fileLocation)) { source =>
            source.getLines().toList
          })
          .flatMap(mayBeFileContent =>
            mayBeFileContent match {
              case Success(fileContent) =>
                ZIO.succeed(
                  assert(fileContent)(equalTo(List(newContent, "World")))
                )
              case _ => ZIO.fail(new Exception("File not found"))
            }
          )
      },
      test("Delete file") {
        invokePrivateMethod[ZIO[Any, Throwable, Boolean]](
          fileManager,
          "deleteFile"
        ) *> ZIO
          .attempt(new File(fileLocation))
          .flatMap(file => ZIO.succeed(assert(file.exists())(equalTo(false))))
      }
    ) @@ sequential @@ beforeAll(beforeAllHook)
}
