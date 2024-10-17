package com.raunakjodhawat

import zio.cli.{Args, Options}

import java.time.LocalDate
object Todos {
  final case class Create(todo: List[String], date: LocalDate)
      extends Subcommand
  final case class Get(date: Option[LocalDate]) extends Subcommand
  final case class Update(oldTodo: String, newTodo: String) extends Subcommand
  final case class Delete(todo: String) extends Subcommand

  val dateOption: Options[LocalDate] = Options
    .localDate("date")
    .alias("d")
    .withDefault(LocalDate.now()) ?? "Date of the todo"

  val todo: Args[List[String]] = Args.text("todo").repeat ?? "todo task"
}
