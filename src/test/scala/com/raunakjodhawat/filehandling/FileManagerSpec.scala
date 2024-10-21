package com.raunakjodhawat.filehandling

import zio.test.Assertion.equalTo
import zio.test._
import zio.{Scope, ZIO}
import zio.test.TestAspect.{beforeAll, sequential}
import zio.test.{Spec, TestEnvironment}
import zio.test.junit.JUnitRunnableSpec

import java.io.File
import scala.util.{Success, Using}

object FileManagerSpec extends JUnitRunnableSpec {
  val fileLocation = "src/test/resources/file.txt"
  val fileManager = new FileManager(fileLocation)
  val beforeAllHook: ZIO[Any, Throwable, Unit] = ZIO.attempt {
    val file = new java.io.File(fileLocation)
    if (file.exists()) file.delete()
  }.unit
  override def spec: Spec[TestEnvironment with Scope, Throwable] =
    suite("File Manager Spec")(
      test("File does not exists") {
        fileManager.fileExists.flatMap(exists =>
          ZIO.succeed(assert(exists)(equalTo(false)))
        )
      },
      test("Create file") {
        fileManager.createIfDoesNotExist *> ZIO
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
        fileManager.deleteFile *> ZIO
          .attempt(new File(fileLocation))
          .flatMap(file => ZIO.succeed(assert(file.exists())(equalTo(false))))
      }
    ) @@ sequential @@ beforeAll(beforeAllHook)
}
