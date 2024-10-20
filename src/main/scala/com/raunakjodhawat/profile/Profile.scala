package com.raunakjodhawat.profile

import com.raunakjodhawat.filehandling.FileManager
import com.raunakjodhawat.filehandling.FileManagerConfig.{
  fileLocation,
  tempFileLocation
}
import com.raunakjodhawat.utils.Subcommand
import zio.Console.printLine
import zio.ZIO
import zio.cli.Options

object Profile {
  final case class Create(profileName: String) extends Subcommand
  final case class Get() extends Subcommand
  final case class Update(oldProfileName: String, newProfileName: String)
      extends Subcommand
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

  val fileManagerConfig = new FileManager(fileLocation)
  val tempManagerConfig = new FileManager(tempFileLocation)
  val pm = new ProfileManager(fileManagerConfig, tempManagerConfig)
  object Operations {
    def get: ZIO[Any, Throwable, Any] = pm.getAllProfileNames
      .flatMap(profiles => {
        ZIO.ifZIO(ZIO.succeed(profiles.isEmpty))(
          printLine("Warning! No profiles found"),
          printLine(profiles.mkString("\n"))
        )
      })

    def create(name: String): ZIO[Any, Throwable, Any] = pm
      .createProfile(name)
      .flatMap(_ => printLine(s"Profile '$name' created successfully"))
      .catchAll(_ => printLine(s"Profile '$name' already exists"))

    def delete(name: String): ZIO[Any, Throwable, Any] = pm
      .deleteProfile(name)
      .flatMap(_ => printLine(s"Profile '$name' deleted successfully"))
      .catchAll(e => printLine(e.getMessage))

    def update(oldName: String, newName: String): ZIO[Any, Throwable, Any] =
      pm
        .updateProfile(oldName, newName)
        .flatMap(_ => printLine(s"Profile '$oldName' updated to '$newName'"))
        .catchAll(_ => printLine(s"Profile '$oldName' does not exist"))
  }
}
