package com.raunakjodhawat.profile

import com.raunakjodhawat.filehandling.FileManager
import com.raunakjodhawat.profile.ProfileException.{
  ProfileAlreadyExistsException,
  ProfileDefaultDeleteException,
  ProfileDoesNotExistException
}
import zio.ZIO

class ProfileManager(
    fConfig: FileManager,
    tempConfig: FileManager
) {
  private def doesProfileExists(
      profileName: String
  ): ZIO[Any, Throwable, Boolean] =
    getAllProfileNames.flatMap(profileNames =>
      ZIO.succeed(profileNames.contains(profileName))
    )

  def getAllProfileNames: ZIO[Any, Throwable, List[String]] =
    fConfig.initialFileSetup *>
      fConfig.getFileContent.foldZIO(
        _ => ZIO.fail(new Exception("Unable to open file")),
        lines => {
          val profileNames = lines
            .filter(x => x.startsWith("[") && x.endsWith("]"))
            .map(_.drop(1).dropRight(1))
          ZIO.succeed(profileNames)
        }
      )

  def createProfile(profileName: String): ZIO[Any, Throwable, Unit] =
    fConfig.initialFileSetup *> ZIO.ifZIO(doesProfileExists(profileName))(
      ZIO.fail(new ProfileAlreadyExistsException(profileName)),
      fConfig.appendToFile(List[String](s"[$profileName]"))
    )

  def updateProfile(
      oldName: String,
      newName: String
  ): ZIO[Any, Throwable, Unit] = {
    fConfig.initialFileSetup *> doesProfileExists(oldName)
      .zipPar(doesProfileExists(newName))
      .flatMap({
        case (true, false) =>
          fConfig.updateFile(s"[$oldName]", s"[$newName]")
        case (true, true) =>
          ZIO.fail(new ProfileAlreadyExistsException(newName))
        case (false, _) =>
          ZIO.fail(new ProfileDoesNotExistException(oldName))
      })
  }

  def deleteProfile(profileName: String): ZIO[Any, Throwable, Unit] = {
    def updateLines(lines: List[String]): List[String] = {
      val profileIndex = lines.indexWhere(_.trim == s"[$profileName]")
      val (before, after) = lines.splitAt(profileIndex)
      val droppedLines = after.drop(1).takeWhile(line => !line.startsWith("["))
      before ++ after.drop(droppedLines.size + 1)
    }
    fConfig.initialFileSetup *> getAllProfileNames.flatMap(profileNames =>
      if (profileNames.length == 1) {
        ZIO.fail(new ProfileDefaultDeleteException(profileName))
      } else if (!profileNames.contains(profileName)) {
        ZIO.fail(new ProfileDoesNotExistException(profileName))
      } else {
        fConfig.getFileContent.flatMap(lines => {
          tempConfig.appendToFile(updateLines(lines))
        })
      }
    )
  }
}
