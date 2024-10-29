package com.raunakjodhawat.profile

import com.raunakjodhawat.utils.{Subcommand, Utils}
import zio.cli.{Args, Command, Options}

object ProfileConfig {
  final case class Create(profileName: String) extends Subcommand
  final case class Get() extends Subcommand
  final case class Update(oldProfileName: String, newProfileName: String)
      extends Subcommand
  final case class Delete(profileName: String) extends Subcommand

  private val getCommand: Command[Subcommand] = Command(
    name = "get",
    options = Options.none
  ).map(_ => Get())

  private val createCommand: Command[Subcommand] = Command(
    name = "create",
    options = Utils.profileNameOption
  ).map(profileName => Create(profileName))

  private val updateCommand: Command[Subcommand] = Command(
    name = "update",
    options = Utils.oldOption ++ Utils.newOption
  ).map { case (oldValue, newValue) => Update(oldValue, newValue) }

  private val deleteCommand: Command[Subcommand] = Command(
    name = "delete",
    options = Utils.profileNameOption
  ).map(profileName => Delete(profileName))

  val profileTask: Command[Subcommand] =
    Command("profile", options = Options.none, args = Args.none)
      .subcommands(getCommand, createCommand, updateCommand, deleteCommand)
}
