package com.raunakjodhawat

import zio.cli.{Args, Command, Exists, HelpDoc, Options}

object Profile {
  final case class Create(profileName: String) extends Subcommand
  final case class Get() extends Subcommand
  final case class Update(profileName: String) extends Subcommand
  final case class Delete(profileName: String) extends Subcommand

  val nameOption: Options[String] =
    Options
      .text("name")
      .alias("n")
      .withDefault("default") ?? "Name of the profile"

  val isProfileTaskOption: Options[Boolean] =
    Options
      .boolean("profile")
      .alias("p")
      .withDefault(false) ?? "Name of the profile"
}
