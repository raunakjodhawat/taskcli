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
import zio.ZIO
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
  private val task: Command[(Boolean, Subcommand)] =
    Command("task", options = ProfileConfig.isProfileTaskOption, Args.none)
      .subcommands(getCommand, createCommand, updateCommand, deleteCommand)

  private def mapSubcommand(
      isProfileTask: Boolean,
      subcommand: Subcommand
  ): Subcommand = {
    (isProfileTask, subcommand) match {
      case (true, ProfileConfig.Get())        => ProfileConfig.Get()
      case (true, ProfileConfig.Create(name)) => ProfileConfig.Create(name)
      case (true, ProfileConfig.Update(oldName, newName)) =>
        ProfileConfig.Update(oldName, newName)
      case (true, ProfileConfig.Delete(name))  => ProfileConfig.Delete(name)
      case (false, TodoConfig.Get(name, date)) => TodoConfig.Get(name, date)
      case (false, TodoConfig.Create(name, date, todo)) =>
        TodoConfig.Create(name, date, todo)
    }
  }

  private def executeCommand(
      subcommand: Subcommand
  ): ZIO[Any, Nothing, Any] = {
    subcommand match {
      case ProfileConfig.Get()        => Config.profile.get.orDie
      case ProfileConfig.Create(name) => Config.profile.create(name).orDie
      case ProfileConfig.Update(oldName, newName) =>
        Config.profile.update(oldName, newName).orDie
      case ProfileConfig.Delete(name) => Config.profile.delete(name).orDie
      case TodoConfig.Get(name, date) => Config.todo.get(name, date).orDie
      case TodoConfig.Create(name, date, todo) =>
        Config.todo.create(name, todo, date).orDie
    }
  }

  private val taskCommand: Command[Subcommand] = task.map {
    case (isProfileTask, subcommand) => mapSubcommand(isProfileTask, subcommand)
  }

  val cliApp: CliApp[Any, Nothing, Subcommand] = CliApp.make(
    name = "Task CLI",
    version = "0.0.1",
    summary = text("a task manager for your daily todos"),
    command = taskCommand
  )(executeCommand)
}
