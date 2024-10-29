package com.raunakjodhawat.utils

import zio.cli.Options

object Utils {
  val oldOption: Options[String] =
    Options
      .text("old")
      .withDefault(
        "default"
      ) ?? "Old name of the profile or old todo description"
  val newOption: Options[String] =
    Options
      .text("new")
      .withDefault(
        "default"
      ) ?? "new name of the profile or new todo description"

  val isProfileTaskOption: Options[Boolean] =
    Options
      .boolean("profile")
      .alias("p")
      .withDefault(false) ?? "Name of the profile"

  val profileNameOption: Options[String] =
    Options
      .text("name")
      .alias("n")
      .withDefault("default") ?? "Name of the profile"
}
