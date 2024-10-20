package com.raunakjodhawat.todo

import zio.Console.printLine
import zio.ZIO

import java.time.LocalDate
class Todo(manager: TodoManager) {

  def get(
      profileName: Option[String],
      date: Option[LocalDate]
  ): ZIO[Any, Throwable, Any] = manager
    .getTaskWithDateAndProfileName(profileName, date)
    .flatMap(todos => ZIO.succeed(todos.foreach(x => printLine(x))))
    .catchAll(e => printLine(e.getMessage))

  def create(
      profileName: Option[String],
      date: Option[LocalDate],
      todo: List[String]
  ): ZIO[Any, Throwable, Any] = manager
    .createTodo(profileName, date, todo)
    .flatMap(_ => printLine("Task created successfully"))
    .catchAll(e => printLine(e.getMessage))
}
