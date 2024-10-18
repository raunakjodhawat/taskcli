package com.raunakjodhawat.utils

import com.raunakjodhawat.profile.Profile
import com.raunakjodhawat.todo.Todo
import zio.cli.{Command, Options}

object Utils {
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
      Profile.isProfileTaskOption ++ Profile.nameOption ++ Todo.dateOption,
    args = Todo.todo
  ).map { case ((isProfileTask: Boolean, name, date), todo) =>
    if (isProfileTask) Profile.Create(name)
    else Todo.Create(todo, date)
  }

  val getCommand: Command[Subcommand] = Command(
    name = "get",
    options = Profile.isProfileTaskOption ++ Todo.dateOption
  ).map { case (isProfileTask, date) =>
    if (isProfileTask) Profile.Get()
    else Todo.Get(Some(date))
  }

  val updateCommand: Command[Subcommand] = Command(
    name = "update",
    options = Profile.isProfileTaskOption ++ oldOption ++ newOption
  ).map { case (isProfileTask, oldValue, newValue) =>
    if (isProfileTask) Profile.Update(oldValue, newValue)
    else Todo.Update(oldValue, newValue)
  }

  val deleteCommand: Command[Subcommand] = Command(
    name = "delete",
    options = Profile.isProfileTaskOption ++ Profile.nameOption
  ).map { case (isProfileTask, value) =>
    if (isProfileTask) Profile.Delete(value)
    else Todo.Delete(value)
  }

}
