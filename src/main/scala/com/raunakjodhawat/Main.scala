package com.raunakjodhawat

import com.raunakjodhawat.filehandling.{FileManager, FileManagerConfig}
import com.raunakjodhawat.profile.{Profile, ProfileConfig, ProfileManager}
import com.raunakjodhawat.todo.{Todo, TodoConfig, TodoManager}
import com.raunakjodhawat.utils.{Subcommand, Utils}
import zio.ZIO
import zio.cli.HelpDoc.Span.text
import zio.cli._

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

  private val taskCommand: Command[Subcommand] =
    Command("task", options = Utils.isProfileTaskOption, Args.none)
      .subcommands(ProfileConfig.profileTask, TodoConfig.todoTask)
      .map { case (isProfileTask, subcommand) =>
        if (isProfileTask) subcommand.asInstanceOf[ProfileConfig.profileTask]
        else subcommand.asInstanceOf[TodoConfig.Subcommand]
      }

  val cliApp: CliApp[Any, Nothing, Subcommand] = CliApp.make(
    name = "Task CLI",
    version = "0.0.1",
    summary = text("a task manager for your daily todos"),
    command = taskCommand
  )(executeCommand)
}
