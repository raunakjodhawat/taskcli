package com.raunakjodhawat.todo

import com.raunakjodhawat.utils.{Subcommand, Utils}
import zio.cli.{Args, Command, Options}

import java.time.LocalDate

object TodoConfig {
  final case class Create(
      profileName: String,
      todo: List[String],
      date: LocalDate
  ) extends Subcommand
  final case class Get(profileName: String, date: LocalDate) extends Subcommand
  final case class Update(oldTodo: String, newTodo: String) extends Subcommand
  final case class Delete(todo: String) extends Subcommand

  private val dateOption: Options[LocalDate] = Options
    .localDate("date")
    .alias("d")
    .withDefault(LocalDate.now()) ?? "Date of the todo"

  private val todo: Args[List[String]] = Args.text("todo").repeat ?? "todo task"

  private val getCommand: Command[Subcommand] = Command(
    name = "get",
    options = dateOption ++ Utils.profileNameOption
  ).map { case (date, profileName) => Get(profileName, date) }

  private val createCommand: Command[Subcommand] = Command(
    name = "create",
    options = Utils.profileNameOption ++ dateOption,
    args = todo
  ).map { case ((profileName, date), todo) =>
    Create(profileName, todo, date)
  }
  val todoTask: Command[Subcommand] =
    Command("todo", options = Options.none, Args.none)
      .subcommands(getCommand, createCommand)
}
