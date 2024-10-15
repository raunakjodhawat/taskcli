package com.raunakjodhawat

import com.raunakjodhawat.CommonUtils.{createCommand, getCommand}
import com.raunakjodhawat.filehandling.FileManager
import zio.Console.printLine
import zio.cli.HelpDoc.Span.text
import zio.cli._

object Main extends ZIOCliDefault {

  /** task create --p (profileName) or --name
    *      - creates profile if it does not exist
    * task get --p
    *      - gets all the profiles
    * task update --p (old) (new) or --old & --new
    *      - updates the profile name with new name, if old name exists
    * task delete --p (profileName) or --name
    *     - deletes the profile if it exists
    */

  val task: Command[Subcommand] =
    Command("task", Options.none, Args.none)
      .subcommands(createCommand, getCommand)

  val cliApp = CliApp.make(
    name = "Task CLI",
    version = "0.0.1",
    summary = text("a task manager for your daily todos"),
    command = task
  ) {
    case Profile.Create(name) =>
      FileManager
        .createProfile(name)
        .map { _ =>
          printLine(s"Profile $name created successfully")
        }
        .mapError { e =>
          printLine(s"Error creating profile: ${e.getMessage}")
        }
    case Todos.Create(todo, date) =>
      FileManager.createTodoForAProfile("raunak", todo, date).map { case _ =>
        printLine(s"Todo $todo created successfully")
      }
  }
}
