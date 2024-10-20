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
  def fileExists: ZIO[Any, Throwable, Boolean] =
    fileZio.flatMap(f => ZIO.succeed(f.exists()))

  /** Creates a new file at the specified location.
    *
    * @return A `ZIO` effect that, when executed, will return `true` if the file was created successfully, `false` otherwise.
    */
  private def create: ZIO[Any, Throwable, Boolean] =
    fileZio
      .flatMap(f => ZIO.succeed(f.createNewFile()))

  /** Deletes the file at the specified location.
    *
    * @return A `ZIO` effect that, when executed, will return `true` if the file was deleted successfully, `false` otherwise.
    */
  def deleteFile: ZIO[Any, Throwable, Boolean] =
    fileZio.flatMap(f => ZIO.attempt(f.delete()))

  /** Appends the specified content to the file at the specified location.
    *
    * @param content: List[String] - The content to append to the file.
    * @return
    */
  def appendToFile(content: List[String]): ZIO[Any, Throwable, Unit] =
    ZIO.attempt {
      Using(new PrintWriter(new java.io.FileWriter(fileLocation, true))) {
        writer =>
          content.foreach(writer.println)
      }
    }

  /** Creates a new file at the specified location if it does not already exist.
    *
    * @return A `ZIO` effect that, when executed, will create a new file if it does not already exist.
    */
  def createIfDoesNotExist: ZIO[Any, Throwable, Unit] =
    fileExists.flatMap(exists => ZIO.when(!exists)(create)).unit

  def updateFile(
      oldContent: String,
      newContent: String
  ): ZIO[Any, Throwable, Unit] = ZIO.attempt {
    Using(scala.io.Source.fromFile(fileLocation)) { source =>
      val lines = source.getLines().toList
      val updatedLines = lines.map {
        case line if line == oldContent => newContent
        case line                       => line
      }
      Using(new PrintWriter(fileLocation)) { writer =>
        updatedLines.foreach(writer.println)
      }
    }
  }
}
