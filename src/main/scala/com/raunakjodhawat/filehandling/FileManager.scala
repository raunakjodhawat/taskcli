package com.raunakjodhawat.filehandling

import zio._
import zio.stream._

import java.io.{File, IOException}
import java.time.LocalDate
import scala.util.{Failure, Success, Using}

object FileManager {

  /** file format
    * [profileName]
    * todo1, date1
    * todo2, date2
    */
  val fileLocation: String = FileManagerConfig.fileLocation
  val tempFileLocation: String = FileManagerConfig.tempFileLocation
  def createFileIfDoesNotExist: ZIO[Any, Throwable, Unit] = {
    ZIO.attempt(new File(fileLocation).exists()).flatMap { exists =>
      if (exists) ZIO.unit
      else {
        ZIO
          .attempt(new File(fileLocation).createNewFile())
          .unit
          .orElseFail(
            new IOException(s"Error creating file at location $fileLocation")
          )
      }
    }
  }
  def appendToTempFile(append: List[String]): ZIO[Any, Throwable, Unit] = {
    ZIO.attempt {
      Using(new java.io.PrintWriter(tempFileLocation)) { writer =>
        append.foreach(writer.println)
      }
    }
  }
  def getAllProfileNames: ZIO[Any, Throwable, Chunk[String]] = {
    createFileIfDoesNotExist *>
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

  /** Auto clearing of tasks after 30 days
    * task clearCache
    */
  /** Delete a profile and all the associated tasks of that profile
    * all tasks are transferred to a temp file, with an expiry date (to be cleared after 30 days)
    *
    * temp file format
    * start: [expiryDate]
    * [profileName]
    * todo1, date1
    * todo2, date2
    * end: [expiryDate]
    *
    * If you want to delete the profile and task from temp file as well
    * -- task delete --profile profileName --all
    *
    * @param profileName
    * @return
    */
  def deleteProfile(profileName: String): ZIO[Any, Throwable, Unit] = {
    getAllProfileNames.flatMap { profileNames =>
      ZIO.whenZIO(ZIO.succeed(!profileNames.contains(profileName))) {
        ZIO.fail(
          new NoSuchElementException(s"Profile '$profileName' does not exist.")
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
            appendToTempFile(tempLines) *> ZIO.attempt {
              Using(new java.io.PrintWriter(fileLocation)) { writer =>
                newLines.foreach(writer.println)
              }
            }
          case Failure(exception) => ZIO.fail(exception)
        }
    }
  }
  def getAllTodosForAProfile(
      profileName: String
  ): ZIO[Any, Throwable, List[(String, String)]] = {
    ZIO.attempt {
      Using(scala.io.Source.fromFile(fileLocation)) { source =>
        val lines = source.getLines().toList
        val profileIndex = lines.indexWhere(_.trim == s"[$profileName]")
        if (profileIndex == -1) List.empty
        else {
          lines
            .drop(profileIndex + 1)
            .takeWhile(line => !line.startsWith("["))
            .map { line =>
              val Array(todo, date) = line.split(",").map(_.trim)
              (todo, date)
            }
        }
      }.getOrElse(List.empty)
    }
  }

  def createTodoForAProfile(
      profileName: String,
      todo: List[String],
      date: LocalDate
  ): ZIO[Any, Throwable, Unit] = {
    for {
      todos <- getAllTodosForAProfile(profileName)
      newTodo = s"$todo, $date"
      allLines <- ZIO.attempt {
        Using(scala.io.Source.fromFile(fileLocation)) { source =>
          source.getLines().toList
        }.getOrElse(List.empty)
      }
      profileIndex = allLines.indexWhere(_.trim == s"[$profileName]")
      _ <-
        if (profileIndex == -1)
          ZIO.fail(
            new NoSuchElementException(
              s"Profile '$profileName' does not exist."
            )
          )
        else ZIO.unit
      newLines = {
        val (before, after) = allLines.splitAt(profileIndex + 1)
        before ++ newTodo.split("\n").toList ++ after.dropWhile(line =>
          !line.startsWith("[")
        )
      }
      _ <- ZIO.attempt {
        Using(new java.io.PrintWriter(fileLocation)) { writer =>
          newLines.foreach(writer.println)
        }
      }
    } yield ()
  }

  def updateProfileName(
      oldProfileName: String,
      newProfileName: String
  ): ZIO[Any, Throwable, Unit] = {
    for {
      allLines <- ZIO.attempt {
        Using(scala.io.Source.fromFile(fileLocation)) { source =>
          source.getLines().toList
        }.getOrElse(List.empty)
      }
      oldProfileIndex = allLines.indexWhere(_.trim == s"[$oldProfileName]")
      _ <-
        if (oldProfileIndex == -1)
          ZIO.fail(
            new NoSuchElementException(
              s"Profile '$oldProfileName' does not exist."
            )
          )
        else ZIO.unit
      newProfileIndex = allLines.indexWhere(_.trim == s"[$newProfileName]")
      _ <-
        if (newProfileIndex != -1)
          ZIO.fail(
            new IllegalArgumentException(
              s"Profile '$newProfileName' already exists."
            )
          )
        else ZIO.unit
      newLines = allLines.map { line =>
        if (line == s"[$oldProfileName]") s"[$newProfileName]"
        else line
      }
      _ <- ZIO.attempt {
        Using(new java.io.PrintWriter(fileLocation)) { writer =>
          newLines.foreach(writer.println)
        }
      }
    } yield ()
  }

  def getAllTodos: ZIO[Any, Throwable, List[(String, String)]] = {
    ZIO.attempt {
      Using(scala.io.Source.fromFile(fileLocation)) { source =>
        val lines = source.getLines().toList
        lines
          .dropWhile(line => !line.startsWith("["))
          .flatMap { line =>
            lines
              .dropWhile(_ != line)
              .drop(1)
              .takeWhile(line => !line.startsWith("["))
              .map { line =>
                val Array(todo, date) = line.split(",").map(_.trim)
                (todo, date)
              }
          }
      }.getOrElse(List.empty)
    }
  }

  def getAllTodoForToday: ZIO[Any, Throwable, List[(String, String)]] = {
    val today = java.time.LocalDate.now().toString
    getAllTodos.map(_.filter { case (_, date) => date == today })
  }
}
