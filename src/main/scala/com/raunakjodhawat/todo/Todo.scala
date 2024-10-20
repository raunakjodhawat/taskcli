package com.raunakjodhawat.todo

import zio.Console.printLine
import zio.ZIO

import java.time.LocalDate
class Todo(manager: TodoManager) {

  def get(
      profileName: Option[String],
      date: Option[LocalDate]
  ): ZIO[Any, Throwable, Any] = manager
    .getTodo(profileName, date)
    .flatMap(todos => ZIO.succeed(todos.foreach(x => printLine(x))))
    .catchAll(e => printLine(e.getMessage))
}
