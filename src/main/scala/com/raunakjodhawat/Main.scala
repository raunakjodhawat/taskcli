package com.raunakjodhawat

import com.raunakjodhawat.filehandling.{FileManager, FileManagerConfig}
import com.raunakjodhawat.utils.Utils.{
  createCommand,
  deleteCommand,
  getCommand,
  updateCommand
}
import com.raunakjodhawat.profile.{Profile, ProfileConfig, ProfileManager}
import com.raunakjodhawat.todo.{Todo, TodoConfig, TodoManager}
import com.raunakjodhawat.utils.Subcommand
import zio.cli.HelpDoc.Span.text
import zio.cli._

import java.time.LocalDate

object Config {
  private val fileManager = new FileManager(FileManagerConfig.fileLocation)
  private val tempFileManager = new FileManager(
    FileManagerConfig.tempFileLocation
  )
  private val profileManager = new ProfileManager(fileManager, tempFileManager)
  val profile = new Profile(profileManager)
  private val todoManager =
    new TodoManager(fileManager, tempFileManager, profileManager)
  val todo = new Todo(todoManager)
}
object Main extends ZIOCliDefault {

  private val task: Command[Subcommand] =
    Command("task", Options.none, Args.none)
      .subcommands(createCommand, getCommand, updateCommand, deleteCommand)

  val cliApp: CliApp[Any, Nothing, Subcommand] = CliApp.make(
    name = "Task CLI",
    version = "0.0.1",
    summary = text("a task manager for your daily todos"),
    command = task
  ) {
    case ProfileConfig.Get() =>
      Config.profile.get.orDie
    case ProfileConfig.Create(name) =>
      Config.profile.create(name).orDie
    case ProfileConfig.Delete(name) =>
      Config.profile.delete(name).orDie
    case ProfileConfig.Update(oldName, newName) =>
      Config.profile.update(oldName, newName).orDie
    case TodoConfig.Get(profileName, date) =>
      Config.todo.get(profileName, date).orDie
    case TodoConfig.Create(profileName, todo, date: Option[LocalDate]) =>
      Config.todo.create(profileName, date, todo).orDie
  }
}
