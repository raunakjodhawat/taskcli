package com.raunakjodhawat.profile

import com.raunakjodhawat.filehandling.FileManager
import com.raunakjodhawat.filehandling.FileManagerConfig.{
  fileLocation,
  tempFileLocation
}
import zio.stream.{ZPipeline, ZStream}
import zio.{Chunk, ZIO}

import java.io.File
import scala.util.{Failure, Success, Using}

class ProfileManager(
    fConfig: FileManager,
    tempConfig: FileManager
) {

  def getAllProfileNames: ZIO[Any, Throwable, Chunk[String]] = {
    fConfig.createIfDoesNotExist *>
      ZStream
        .fromFile(new File(fileLocation))
        .via(ZPipeline.utf8Decode)
        .via(ZPipeline.splitLines)
        .filter(x => x.startsWith("[") && x.endsWith("]"))
        .map(_.drop(1).dropRight(1))
        .runCollect
  }
  def createProfile(profileName: String): ZIO[Any, Throwable, Unit] = {
    getAllProfileNames
      .flatMap(profileNames => {
        ZIO.when(profileNames.contains(profileName)) {
          ZIO.fail(
            new IllegalArgumentException(
              s"Profile '$profileName' already exists."
            )
          )
        } *> ZIO.attempt {
          Using(
            new java.io.PrintWriter(
              new java.io.FileOutputStream(fileLocation, true)
            )
          ) { writer =>
            writer.println(s"[$profileName]")
          }
        }
      })
  }
  def updateProfile(
      oldName: String,
      newName: String
  ): ZIO[Any, Throwable, Unit] = {
    getAllProfileNames.flatMap(profileNames => {
      ZIO.when(!profileNames.contains(oldName)) {
        ZIO.fail(
          new NoSuchElementException(s"Profile '$oldName' does not exist.")
        )
      } *> ZIO.when(profileNames.contains(newName)) {
        ZIO.fail(
          new IllegalArgumentException(s"Profile '$newName' already exists.")
        )
      } *> ZIO.attempt {
        Using(scala.io.Source.fromFile(fileLocation)) { source =>
          val lines = source.getLines().toList
          val newLines = lines.map { line =>
            if (line == s"[$oldName]") s"[$newName]"
            else line
          }
          Using(new java.io.PrintWriter(fileLocation)) { writer =>
            newLines.foreach(writer.println)
          }
        }
      }
    })
  }
  def deleteProfile(profileName: String): ZIO[Any, Throwable, Unit] = {
    getAllProfileNames.flatMap { profileNames =>
      ZIO.whenZIO(ZIO.succeed(!profileNames.contains(profileName))) {
        ZIO.fail(
          new NoSuchElementException(
            s"Profile '$profileName' does not exist"
          )
        )
      } *>
        ZIO.whenZIO(
          ZIO.succeed(
            profileNames.length == 1 && profileNames(0) == profileName
          )
        ) {
          ZIO.fail(
            new IllegalStateException(
              s"Profile '$profileName' can't be deleted, as it's the default"
            )
          )
        } *> ZIO
          .attempt {
            Using(scala.io.Source.fromFile(fileLocation)) { source =>
              val lines = source.getLines().toList
              val profileIndex = lines.indexWhere(_.trim == s"[$profileName]")
              if (profileIndex == -1) {
                throw new NoSuchElementException(
                  s"Profile '$profileName' does not exist."
                )
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
