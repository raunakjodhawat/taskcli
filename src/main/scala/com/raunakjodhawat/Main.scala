package com.raunakjodhawat

import com.raunakjodhawat.filehandling.{FileManager, FileManagerConfig}
import com.raunakjodhawat.utils.Utils.{
  createCommand,
  deleteCommand,
  getCommand,
  updateCommand
}
import com.raunakjodhawat.profile.{Profile, ProfileConfig, ProfileManager}
import com.raunakjodhawat.utils.Subcommand
import zio.cli.HelpDoc.Span.text
import zio.cli._

object Main extends ZIOCliDefault {
  private val fileManager = new FileManager(FileManagerConfig.fileLocation)
  private val tempFileManager = new FileManager(
    FileManagerConfig.tempFileLocation
  )
  private val profileManager = new ProfileManager(fileManager, tempFileManager)
  val profile = new Profile(profileManager)
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
      profile.get.orDie
    case ProfileConfig.Create(name) =>
      profile.create(name).orDie
    case ProfileConfig.Delete(name) =>
      profile.delete(name).orDie
    case ProfileConfig.Update(oldName, newName) =>
      profile.update(oldName, newName).orDie
  }
}
