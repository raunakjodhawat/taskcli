package com.raunakjodhawat.todo

import com.raunakjodhawat.filehandling.FileManager
import com.raunakjodhawat.profile.ProfileManager
import zio.ZIO

import java.time.LocalDate

class TodoManager(
    fConfig: FileManager,
    tempConfig: FileManager,
    profileManager: ProfileManager
) {
  private def getDate: Option[LocalDate] => LocalDate =
    _.getOrElse(LocalDate.now())

  private def getProfileName: Option[String] => ZIO[Any, Throwable, String] = {
    case Some(profileName) => ZIO.succeed(profileName)
    case None =>
      fConfig.getFileContent
        .flatMap(lines => {
          val defaultProfile = lines
            .find(x => x.startsWith("[") && x.endsWith("]"))
            .map(_.drop(1).dropRight(1))
          defaultProfile match {
            case Some(profile) => ZIO.succeed(profile)
            case None =>
              ZIO.fail(new NoSuchElementException("No default profile found"))
          }
        })
  }

  def getTaskWithDateAndProfileName(
      optionalProfileName: Option[String],
      optionalDate: Option[LocalDate]
  ): ZIO[Any, Throwable, List[String]] = {
    val date: LocalDate = getDate(optionalDate)
    getProfileName(optionalProfileName).flatMap { profileName =>
      fConfig.createIfDoesNotExist *>
        fConfig.getFileContent.flatMap(lines => {
          val profileIndex = lines.indexWhere(_.trim == s"[$profileName]")
          val (_, after) = lines.splitAt(profileIndex)
          val droppedLines =
            after.drop(1).takeWhile(line => !line.startsWith("["))
          val todos = droppedLines
            .filter(x => x.endsWith(date.toString))
            .map(x => x.dropRight(date.toString.length + 2))
          ZIO.succeed(todos)
        })
    }
  }

  def createTodo(
      optionalProfileName: Option[String],
      optionalDate: Option[LocalDate],
      todo: List[String]
  ): ZIO[Any, Throwable, Unit] = {
    fConfig.createIfDoesNotExist *> (for {
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
      _ <- fConfig.getFileContent.flatMap(lines => {
        val profileIndex = lines.indexWhere(_.trim == s"[$profileName]")
        val (before, after) = lines.splitAt(profileIndex)
        val newTodoLine = todo.mkString(", ") + s", $date"
        val updatedLines =
          before ++ (after.headOption.toList ++ List(
            newTodoLine
          ) ++ after.tail)
        fConfig.updateFileContent(updatedLines)
      })
    } yield ())
  }
}
