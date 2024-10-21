package com.raunakjodhawat.profile

import com.raunakjodhawat.profile.ProfileException.{
  ProfileAlreadyExistsException,
  ProfileDoesNotExistException
}
import zio.Console.printLine
import zio.ZIO

class Profile(manager: ProfileManager) {

  def get: ZIO[Any, Throwable, Any] = manager.getAllProfileNames.flatMap {
    profiles =>
      ZIO.unless(profiles.isEmpty)(printLine(profiles.mkString("\n")))
  }

  def create(name: String): ZIO[Any, Throwable, Any] = manager
    .createProfile(name)
    .flatMap(_ => printLine(s"Profile '$name' created successfully"))
    .catchAll {
      case e: ProfileDoesNotExistException =>
        printLine(e.getMessage)
      case e: ProfileAlreadyExistsException =>
        printLine(e.getMessage)
      case _ => ZIO.unit
    }

  def delete(name: String): ZIO[Any, Throwable, Any] = manager
    .deleteProfile(name)
    .flatMap(_ => printLine(s"Profile '$name' deleted successfully"))
    .catchAll(e => printLine(e.getMessage))

  def update(oldName: String, newName: String): ZIO[Any, Throwable, Any] =
    manager
      .updateProfile(oldName, newName)
      .flatMap(_ => printLine(s"Profile '$oldName' updated to '$newName'"))
      .catchAll {
        case e: ProfileAlreadyExistsException =>
          printLine(e.getMessage)
        case e: ProfileDoesNotExistException =>
          printLine(e.getMessage)
      }
}
