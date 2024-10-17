package com.raunakjodhawat

import com.raunakjodhawat.utils.Utils.{
  createCommand,
  deleteCommand,
  getCommand,
  updateCommand
}
import com.raunakjodhawat.profile.Profile
import com.raunakjodhawat.utils.Subcommand
import zio.cli.HelpDoc.Span.text
import zio.cli._

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
    case Profile.Get() =>
      Profile.Operations.get.orDie
    case Profile.Create(name) =>
      Profile.Operations.create(name).orDie
    case Profile.Delete(name) =>
      Profile.Operations.delete(name).orDie
    case Profile.Update(oldName, newName) =>
      Profile.Operations.update(oldName, newName).orDie
  }
}
