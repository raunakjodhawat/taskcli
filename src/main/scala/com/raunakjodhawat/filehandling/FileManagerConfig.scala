package com.raunakjodhawat.filehandling

import com.raunakjodhawat.filehandling.FileManager.tempFileLocation
import com.typesafe.config.ConfigFactory
import zio.ZIO

import java.io.{File, IOException, PrintWriter}
import scala.util.Using
object FileManagerConfig {
  private val configLayer = ConfigFactory.load().getConfig("fileManager")
  val fileLocation: String = configLayer.getString("fileLocation")
  val tempFileLocation: String = configLayer.getString("tempFileLocation")
  def createFileIfDoesNotExist: ZIO[Any, Throwable, Unit] = {
    ZIO.attempt(new File(fileLocation).exists()).flatMap { exists =>
      if (exists) ZIO.unit
      else {
        (for {
          _ <- ZIO.attempt(new File(fileLocation).createNewFile())
          _ <- ZIO.attempt {
            Using(new PrintWriter(fileLocation)) { writer =>
              {
                writer.println("[default]")
              }
            }
          }
        } yield ()).unit
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
}
