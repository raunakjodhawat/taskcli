package com.raunakjodhawat.utils

import com.raunakjodhawat.profile.ProfileConfig
import com.raunakjodhawat.todo.{Todo, TodoConfig}
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
      ProfileConfig.isProfileTaskOption ++ ProfileConfig.nameOption ++ TodoConfig.dateOption,
    args = TodoConfig.todo
  ).map { case ((isProfileTask: Boolean, name, date), todo) =>
    if (isProfileTask) ProfileConfig.Create(name)
    else TodoConfig.Create(Some(name), todo, Some(date))
  }

  val getCommand: Command[Subcommand] = Command(
    name = "get",
    options =
      ProfileConfig.isProfileTaskOption ++ TodoConfig.dateOption ++ ProfileConfig.nameOption
  ).map { case (isProfileTask, date, profileName) =>
    if (isProfileTask) ProfileConfig.Get()
    else TodoConfig.Get(Some(profileName), Some(date))
  }

  val updateCommand: Command[Subcommand] = Command(
    name = "update",
    options = ProfileConfig.isProfileTaskOption ++ oldOption ++ newOption
  ).map { case (isProfileTask, oldValue, newValue) =>
    if (isProfileTask) ProfileConfig.Update(oldValue, newValue)
    else TodoConfig.Update(oldValue, newValue)
  }

  val deleteCommand: Command[Subcommand] = Command(
    name = "delete",
    options = ProfileConfig.isProfileTaskOption ++ ProfileConfig.nameOption
  ).map { case (isProfileTask, value) =>
    if (isProfileTask) ProfileConfig.Delete(value)
    else TodoConfig.Delete(value)
  }

}
