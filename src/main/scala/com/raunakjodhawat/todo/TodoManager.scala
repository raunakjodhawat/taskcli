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
  private def createProfileIfDoesNotExist
      : String => ZIO[Any, Throwable, String] = { profileName =>
    ZIO.ifZIO(profileManager.doesProfileExists(profileName))(
      onTrue = ZIO.succeed(profileName),
      onFalse = profileManager.createProfile(profileName).as(profileName)
    )
  }

  def getTask(
      profileName: String,
      date: LocalDate
  ): ZIO[Any, Throwable, List[String]] = for {
    profileName <- ZIO.succeed(
      println(profileName)
    ) *> createProfileIfDoesNotExist(profileName)
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
      profileName: String,
      date: LocalDate,
      todo: List[String]
  ): ZIO[Any, Throwable, Unit] = for {
    profileName <- createProfileIfDoesNotExist(profileName)
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
