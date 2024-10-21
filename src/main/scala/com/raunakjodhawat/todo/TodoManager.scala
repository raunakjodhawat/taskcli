package com.raunakjodhawat.todo

import com.raunakjodhawat.filehandling.FileManager
import com.raunakjodhawat.filehandling.FileManagerConfig.{
  fileLocation,
  tempFileLocation
}
import com.raunakjodhawat.profile.ProfileManager
import zio.stream.{ZPipeline, ZStream}
import zio.{Chunk, ZIO}

import java.io.File
import java.time.LocalDate
import scala.util.Using

class TodoManager(
    fConfig: FileManager,
    tempConfig: FileManager,
    profileManager: ProfileManager
) {
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
          .filter(x => x.endsWith(date.toString))
          .runCollect
    }
  }

  import scala.util.Using

  def createTodo(
      optionalProfileName: Option[String],
      optionalDate: Option[LocalDate],
      todo: List[String]
  ): ZIO[Any, Throwable, Unit] = {
    for {
      file <- fConfig.fileZio
      _ <- ZIO.when(!file.exists())(fConfig.createIfDoesNotExist)
      profileName <- getProfileName(optionalProfileName)
      date = getDate(optionalDate)
      profileNames <- profileManager.getAllProfileNames
      _ <- ZIO.when(profileNames.isEmpty)(
        profileManager.createProfile(profileName)
      )
      profileNames <- profileManager.getAllProfileNames
      _ <- ZIO.when(!profileNames.contains(profileName))(
        profileManager.createProfile(profileName)
      )
      _ <- ZIO.attempt {
        Using(scala.io.Source.fromFile(fileLocation)) { source =>
          val lines = source.getLines().toList
          val (before, after) = lines.span(line => line != s"[$profileName]")
          val newTodoLine = todo.mkString(", ") + s", $date"
          val updatedLines =
            before ++ (after.headOption.toList ++ List(
              newTodoLine
            ) ++ after.tail)
          Using(new java.io.PrintWriter(fileLocation)) { writer =>
            updatedLines.foreach(writer.println)
          }
        }
      }
    } yield ()
  }
}
