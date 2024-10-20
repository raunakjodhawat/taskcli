package com.raunakjodhawat.filehandling

import zio.ZIO

import java.io.{File, PrintWriter}
import scala.util.Using

class FileManager(fileLocation: String) {

  val fileZio = ZIO.attempt(new File(fileLocation))

  /** Checks if the file at the specified location exists.
    *
    * @return A `ZIO` effect that, when executed, will return `true` if the file exists, `false` otherwise.
    */
  private def fileExists: ZIO[Any, Throwable, Boolean] =
    fileZio.flatMap(f => ZIO.succeed(f.exists()))

  private def create: ZIO[Any, Throwable, Boolean] =
    fileZio
      .flatMap(f => ZIO.succeed(f.createNewFile()))

  def deleteFile: ZIO[Any, Throwable, Boolean] =
    fileZio.flatMap(f => ZIO.attempt(f.delete()))
  def appendToFile(content: List[String]): ZIO[Any, Throwable, Unit] =
    ZIO.attempt {
      Using(new PrintWriter(fileLocation)) { writer =>
        content.foreach(writer.println)
      }
    }

  def createIfDoesNotExist: ZIO[Any, Throwable, Unit] =
    fileExists.flatMap(exists => ZIO.when(!exists)(create)).unit
}
