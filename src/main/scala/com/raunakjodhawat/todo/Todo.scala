package com.raunakjodhawat.todo

import zio.Console.printLine
import zio.ZIO

import java.time.LocalDate
class Todo(manager: TodoManager) {

  def get(
      profileName: String,
      date: LocalDate
  ): ZIO[Any, Throwable, Any] = manager
    .getTask(profileName, date)
    .flatMap(todos => ZIO.succeed(todos.foreach(x => printLine(x))))
    .catchAll(e => printLine(e.getMessage))

  def create(
      profileName: String,
      date: LocalDate,
      todo: List[String]
  ): ZIO[Any, Throwable, Any] = manager
    .createTodo(profileName, date, todo)
    .flatMap(_ => printLine("Task created successfully"))
    .catchAll(e => printLine(e.getMessage))
}
