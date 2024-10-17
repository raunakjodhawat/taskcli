package com.raunakjodhawat

import com.raunakjodhawat.utils.Utils.{createCommand, deleteCommand, getCommand, updateCommand}
import com.raunakjodhawat.filehandling.FileManager
import com.raunakjodhawat.profile.Profile
import com.raunakjodhawat.todo.Todos
import com.raunakjodhawat.utils.Subcommand
import zio.Console.printLine
import zio.ZIO
import zio.cli.HelpDoc.Span.text
import zio.cli._

object Main extends ZIOCliDefault {
  val task: Command[Subcommand] =
    Command("task", Options.none, Args.none)
      .subcommands(createCommand, getCommand, updateCommand, deleteCommand)

  val cliApp = CliApp.make(
    name = "Task CLI",
    version = "0.0.1",
    summary = text("a task manager for your daily todos"),
    command = task
  ) {
    case Profile.Get() =>
      FileManager.getAllProfileNames.flatMap(profiles => {
        ZIO.ifZIO(ZIO.succeed(profiles.isEmpty))(
          printLine("Warning! No profiles found"),
          printLine(profiles.mkString("\n"))
        )
      })

    case Profile.Create(name) =>
      FileManager
        .createProfile(name)
        .flatMap(_ => printLine(s"Profile '$name' created successfully"))
        .catchAll(_ => printLine(s"Profile '$name' already exists"))
    case Profile.Delete(name) =>
      FileManager
        .deleteProfile(name)
        .flatMap(_ => printLine(s"Profile '$name' deleted successfully"))
        .catchAll(_ => printLine(s"Profile '$name' does not exist"))
    case Todos.Create(todo, date) =>
      FileManager.createTodoForAProfile("raunak", todo, date)

  }
}
