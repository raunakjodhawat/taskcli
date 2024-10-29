package com.raunakjodhawat.filehandling

import zio.ZIO

import java.io.{File, PrintWriter}
import scala.util.{Failure, Success, Using}

class FileManager(fileLocation: String) {

  private val fileZio = ZIO.attempt(new File(fileLocation))

  private def fileExists: ZIO[Any, Throwable, Boolean] =
    fileZio.flatMap(f => ZIO.attempt(f.exists()))

  private def create: ZIO[Any, Throwable, Boolean] =
    fileZio.flatMap(f => ZIO.attempt(f.createNewFile()))

  private def safeCreate: ZIO[Any, Throwable, Boolean] = ZIO.ifZIO(fileExists)(
    onTrue = ZIO.succeed(true),
    onFalse = create
  )

  private def deleteFile: ZIO[Any, Throwable, Boolean] =
    fileZio.flatMap(f => ZIO.attempt(f.delete()))

  def appendToFile(content: List[String]): ZIO[Any, Throwable, Unit] =
    safeCreate *> ZIO.attempt {
      Using(new PrintWriter(new java.io.FileWriter(fileLocation, true))) {
        writer =>
          content.foreach(writer.println)
      }
    }

  def updateFileContent(content: List[String]): ZIO[Any, Throwable, Unit] = {
    deleteFile *> safeCreate *> appendToFile(content)
  }

  def updateFile(
      oldContent: String,
      newContent: String
  ): ZIO[Any, Throwable, Unit] = safeCreate *> ZIO.attempt {
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

  def getFileContent: ZIO[Any, Throwable, List[String]] = safeCreate *> ZIO
    .attempt {
      Using(scala.io.Source.fromFile(fileLocation)) { source =>
        source.getLines().toList
      }
    }
    .foldZIO(
      _ => ZIO.fail(new Exception("Error reading file")),
      {
        case Success(value)     => ZIO.succeed(value)
        case Failure(exception) => ZIO.fail(exception)
      }
    )
}
