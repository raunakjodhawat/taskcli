package com.raunakjodhawat.profile

import zio.Console.printLine
import zio.ZIO

class Profile(manager: ProfileManager) {

  def get: ZIO[Any, Throwable, Any] = manager.getAllProfileNames
    .flatMap(profiles => {
      ZIO.ifZIO(ZIO.succeed(profiles.isEmpty))(
        printLine("Warning! No profiles found"),
        printLine(profiles.mkString("\n"))
      )
    })

  def create(name: String): ZIO[Any, Throwable, Any] = manager
    .createProfile(name)
    .flatMap(_ => printLine(s"Profile '$name' created successfully"))
    .catchAll(_ => printLine(s"Profile '$name' already exists"))

  def delete(name: String): ZIO[Any, Throwable, Any] = manager
    .deleteProfile(name)
    .flatMap(_ => printLine(s"Profile '$name' deleted successfully"))
    .catchAll(e => printLine(e.getMessage))

  def update(oldName: String, newName: String): ZIO[Any, Throwable, Any] =
    manager
      .updateProfile(oldName, newName)
      .flatMap(_ => printLine(s"Profile '$oldName' updated to '$newName'"))
      .catchAll(_ => printLine(s"Profile '$oldName' does not exist"))
}
