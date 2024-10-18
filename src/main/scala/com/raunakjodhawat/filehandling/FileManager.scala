package com.raunakjodhawat.filehandling

import zio._
import java.time.LocalDate
import scala.util.Using

object FileManager {

  /** file format
    * [profileName]
    * todo1, date1
    * todo2, date2
    */
  val fileLocation: String = FileManagerConfig.fileLocation
  val tempFileLocation: String = FileManagerConfig.tempFileLocation

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
