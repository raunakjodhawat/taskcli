package com.raunakjodhawat.todo

import com.raunakjodhawat.filehandling.FileManager
import com.raunakjodhawat.profile.ProfileException.ProfileDoesNotExistException
import com.raunakjodhawat.profile.ProfileManager
import zio.ZIO

import java.time.LocalDate

class TodoManager(
    fConfig: FileManager,
    tempConfig: FileManager,
    profileManager: ProfileManager
) {
  private def getDate: Option[LocalDate] => ZIO[Any, Throwable, LocalDate] =
    ZIO.fromOption(_).orElse(ZIO.succeed(LocalDate.now()))

  private def getProfileName: Option[String] => ZIO[Any, Throwable, String] = {
    case Some(profileName) =>
      profileManager.getAllProfileNames.flatMap { profileNames =>
        if (profileNames.contains(profileName)) ZIO.succeed(profileName)
        else ZIO.fail(new ProfileDoesNotExistException(profileName))
      }
    case None =>
      profileManager.getAllProfileNames.flatMap { profileNames =>
        if (profileNames.nonEmpty) ZIO.succeed(profileNames.head)
        else fConfig.initialFileSetup() *> ZIO.succeed("[default]")
      }
  }

  def getTaskWithDateAndProfileName(
      optionalProfileName: Option[String],
      optionalDate: Option[LocalDate]
  ): ZIO[Any, Throwable, List[String]] = for {
    date <- getDate(optionalDate)
    profileName <- getProfileName(optionalProfileName)
    lines <- fConfig.getFileContent
  } yield {
    val profileIndex = lines.indexWhere(_.trim == s"[$profileName]")
    val (_, after) = lines.splitAt(profileIndex)
    val droppedLines =
      after.drop(1).takeWhile(line => !line.startsWith("["))
    droppedLines
      .filter(x => x.endsWith(date.toString))
      .map(x => x.dropRight(date.toString.length + 2))
  }

  def createTodo(
      optionalProfileName: Option[String],
      optionalDate: Option[LocalDate],
      todo: List[String]
  ): ZIO[Any, Throwable, Unit] = for {
    date <- getDate(optionalDate)
    profileName <- getProfileName(optionalProfileName).orElse {
      ZIO
        .fromOption(optionalProfileName)
        .flatMap(name =>
          fConfig.createIfDoesNotExist *> profileManager.createProfile(
            name
          ) *> ZIO.succeed(name)
        )
        .orElse(
          ZIO.fail(
            new IllegalArgumentException("Profile name does not exist")
          )
        )
    }
    lines <- fConfig.getFileContent
    profileIndex = lines.indexWhere(_.trim == s"[$profileName]")
    (before, after) = lines.splitAt(profileIndex)
    newTodoLine = todo.mkString(", ") + s", $date"
    updatedLines =
      before ++ (after.headOption.toList ++ List(
        newTodoLine
      ) ++ after.tail)
    _ <- fConfig.updateFileContent(updatedLines)
  } yield ()

}
