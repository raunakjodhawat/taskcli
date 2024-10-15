package com.raunakjodhawat

import zio.cli.{Command, Options}

object CommonUtils {
  val oldOption: Options[String] =
    Options
      .text("old")
      .alias("o")
      .withDefault(
        "default"
      ) ?? "Old name of the profile or old todo description"
  val newOption: Options[String] =
    Options
      .text("new")
      .alias("n")
      .withDefault(
        "default"
      ) ?? "new name of the profile or new todo description"
  val createCommand: Command[Subcommand] = Command(
    name = "create",
    options =
      Profile.isProfileTaskOption ++ Profile.nameOption ++ Todos.dateOption,
    args = Todos.todo
  ).map { case ((isProfileTask: Boolean, name, date), todo) =>
    if (isProfileTask) Profile.Create(name)
    else Todos.Create(todo, date)
  }

  val getCommand: Command[Subcommand] = Command(
    name = "get",
    options = Profile.isProfileTaskOption ++ Todos.dateOption
  ).map { case (isProfileTask, date) =>
    if (isProfileTask) Profile.Get()
    else Todos.Get(Some(date))
  }
}
