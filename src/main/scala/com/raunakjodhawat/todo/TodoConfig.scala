package com.raunakjodhawat.todo

import com.raunakjodhawat.utils.Subcommand
import zio.cli.{Args, Options}

import java.time.LocalDate

object TodoConfig {
  final case class Create(
      profileName: Option[String],
      todo: List[String],
      date: Option[LocalDate]
  ) extends Subcommand
  final case class Get(profileName: Option[String], date: Option[LocalDate])
      extends Subcommand
  final case class Update(oldTodo: String, newTodo: String) extends Subcommand
  final case class Delete(todo: String) extends Subcommand

  val dateOption: Options[LocalDate] = Options
    .localDate("date")
    .alias("d")
    .withDefault(LocalDate.now()) ?? "Date of the todo"

  val todo: Args[List[String]] = Args.text("todo").repeat ?? "todo task"
}
