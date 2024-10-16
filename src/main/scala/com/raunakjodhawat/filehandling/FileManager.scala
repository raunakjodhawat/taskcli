package com.raunakjodhawat.filehandling

import zio._
import zio.stream._

import java.io.{File, FileInputStream, IOException}
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import scala.util.Using

object FileManager {

  /** file format
    * [profileName]
    * todo1, date1
    * todo2, date2
    */
  // val sys = System.getProperty("user.dir")
//  val config: Config =
//    ConfigProvider.defaultProvider.load[String]("application")
  val fileLocation: String = "src/main/resources/todos.txt"

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

  def getAllProfileNames: ZIO[Any, Throwable, Chunk[String]] = {
    ZStream
      .fromFile(new File(fileLocation))
      .via(ZPipeline.utf8Decode)
      .via(ZPipeline.splitLines)
      .filter(x => x.startsWith("[") && x.endsWith("]"))
      .map(_.drop(1).dropRight(1))
      .runCollect
  }

  def createProfile(profileName: String): ZIO[Any, Throwable, Unit] = {
    val a = for {
      profileNames <- getAllProfileNames
      _ <-
        if (profileNames.contains(profileName))
          ZIO.fail(
            new IllegalArgumentException(
              s"Profile '$profileName' already exists."
            )
          )
        else ZIO.unit
      _ <- ZStream(
        ZSink
          .fromFile(new File(fileLocation))
          .contramap(c => ZIO.succeed(c.fromIterable(s"[$profileName]")))
      ).runDrain
    } yield ()
    a
  }

  def createTodoForAProfile(
      profileName: String,
      todo: String,
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

  def deleteProfile(profileName: String): ZIO[Any, Throwable, Unit] = {
    for {
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
      newLines = allLines.take(profileIndex) ++ allLines.dropWhile(line =>
        !line.startsWith("[", profileIndex + 1)
      )
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
