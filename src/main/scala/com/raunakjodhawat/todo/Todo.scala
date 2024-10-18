package com.raunakjodhawat.todo

import com.raunakjodhawat.utils.Subcommand
import zio.Console.printLine
import zio.ZIO
import zio.cli.{Args, Options}

import java.time.LocalDate
object Todo {
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

  object Operations {
    def get(
        profileName: Option[String],
        date: Option[LocalDate]
    ): ZIO[Any, Throwable, Any] = TodoManager
      .getTodo(profileName, date)
      .flatMap(todos => ZIO.succeed(todos.foreach(x => printLine(x))))
      .catchAll(e => printLine(e.getMessage))
  }
}
