package com.raunakjodhawat.todo

import com.raunakjodhawat.filehandling.FileManager
import com.raunakjodhawat.filehandling.FileManagerConfig.{
  fileLocation,
  tempFileLocation
}
import zio.stream.{ZPipeline, ZStream}
import zio.{Chunk, ZIO}

import java.io.File
import java.time.LocalDate
import scala.util.Using

class TodoManager(fConfig: FileManager, tempConfig: FileManager) {
  def getDate(optionalDate: Option[LocalDate]): LocalDate =
    optionalDate match {
      case Some(d) => d
      case None    => LocalDate.now()
    }
  def getProfileName(
      optionalProfileName: Option[String]
  ): ZIO[Any, Throwable, String] =
    optionalProfileName match {
      case Some(profileName) => ZIO.succeed(profileName)
      case None =>
        ZIO
          .attempt {
            Using(scala.io.Source.fromFile(fileLocation)) { source =>
              val lines = source.getLines().toList
              lines
                .find(x => x.startsWith("[") && x.endsWith("]"))
                .map(_.drop(1).dropRight(1))
                .mkString("")
            }.getOrElse(
              throw new NoSuchElementException("No default profile found")
            )
          }
          .catchAll { case e: Throwable =>
            ZIO.fail(e)
          }
    }

  def getTaskWithDateAndProfileName(
      optionalProfileName: Option[String],
      optionalDate: Option[LocalDate]
  ): ZIO[Any, Throwable, Chunk[String]] = {
    val date: LocalDate = getDate(optionalDate)
    getProfileName(optionalProfileName).flatMap { profileName =>
      tempConfig.createIfDoesNotExist *>
        ZStream
          .fromFile(new File(tempFileLocation))
          .via(ZPipeline.utf8Decode)
          .via(ZPipeline.splitLines)
          .filter(x => x.startsWith(s"[$profileName]"))
          .filter(x => x.contains(date.toString))
          .runCollect
    }
  }
}
