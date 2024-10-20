package com.raunakjodhawat.profile

import com.raunakjodhawat.filehandling.FileManager
import com.raunakjodhawat.filehandling.FileManagerConfig.fileLocation
import com.raunakjodhawat.profile.ProfileException.{
  ProfileAlreadyExistsException,
  ProfileDefaultDeleteException,
  ProfileDoesNotExistException
}
import zio.stream.{ZPipeline, ZStream}
import zio.{Chunk, ZIO}

import java.io.File
import scala.util.{Failure, Success, Using}

class ProfileManager(
    fConfig: FileManager,
    tempConfig: FileManager
) {

  def createDefaultProfile: ZIO[Any, Throwable, Unit] =
    fConfig.fileExists.flatMap {
      case true => ZIO.unit
      case false =>
        fConfig.createIfDoesNotExist *>
          fConfig.appendToFile(List("[default]"))
    }
  def getAllProfileNames: ZIO[Any, Throwable, Chunk[String]] =
    createDefaultProfile *> fConfig.createIfDoesNotExist *>
      ZStream
        .fromFile(new File(fileLocation))
        .via(ZPipeline.utf8Decode)
        .via(ZPipeline.splitLines)
        .filter(x => x.startsWith("[") && x.endsWith("]"))
        .map(_.drop(1).dropRight(1))
        .runCollect
  def createProfile(profileName: String): ZIO[Any, Throwable, Unit] =
    getAllProfileNames
      .flatMap(profileNames => {
        ZIO.when(profileNames.contains(profileName)) {
          ZIO.fail(
            new ProfileAlreadyExistsException(profileName)
          )
        } *> fConfig.appendToFile(List[String](s"[$profileName]"))
      })
  def updateProfile(
      oldName: String,
      newName: String
  ): ZIO[Any, Throwable, Unit] =
    getAllProfileNames.flatMap { profileNames =>
      ZIO.cond(
        profileNames.contains(oldName),
        (),
        new ProfileDoesNotExistException(oldName)
      ) *> ZIO.cond(
        !profileNames.contains(newName),
        (),
        new ProfileAlreadyExistsException(newName)
      ) *> fConfig.updateFile(s"[$oldName]", s"[$newName]")
    }

  def deleteProfile(profileName: String): ZIO[Any, Throwable, Unit] = {
    getAllProfileNames.flatMap { profileNames =>
      ZIO.whenZIO(ZIO.succeed(!profileNames.contains(profileName))) {
        ZIO.fail(
          new ProfileDoesNotExistException(profileName)
        )
      } *>
        ZIO.whenZIO(
          ZIO.succeed(
            profileNames.length == 1 && profileNames(0) == profileName
          )
        ) {
          ZIO.fail(
            new ProfileDefaultDeleteException(profileName)
          )
        } *> ZIO
          .attempt {
            Using(scala.io.Source.fromFile(fileLocation)) { source =>
              val lines = source.getLines().toList
              val profileIndex = lines.indexWhere(_.trim == s"[$profileName]")
              if (profileIndex == -1) {
                throw new ProfileDoesNotExistException(profileName)
              }
              val (before, after) = lines.splitAt(profileIndex)
              val droppedLines =
                after.drop(1).takeWhile(line => !line.startsWith("["))
              val tempLines = List(
                s"start: ${java.time.LocalDate.now().plusDays(30)}",
                s"[$profileName]"
              ) ++ droppedLines ++ List(
                s"end: ${java.time.LocalDate.now().plusDays(30)}"
              )
              val newLines = before ++ after.drop(droppedLines.size + 1)
              (tempLines, newLines)
            }
          }
          .flatMap {
            case Success((tempLines: List[String], newLines: List[String])) =>
              tempConfig.appendToFile(tempLines) *> ZIO.attempt {
                Using(new java.io.PrintWriter(fileLocation)) { writer =>
                  newLines.foreach(writer.println)
                }
              }
            case Failure(exception) => ZIO.fail(exception)
          }
    }
  }
}
