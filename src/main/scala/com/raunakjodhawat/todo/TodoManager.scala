package com.raunakjodhawat.todo

import com.raunakjodhawat.filehandling.FileManagerConfig.{
  createFileIfDoesNotExist,
  fileLocation
}
import zio.stream.{ZPipeline, ZStream}
import zio.{Chunk, ZIO}

import java.io.File
import java.time.LocalDate

object TodoManager {
  def getTodo(
      profileName: Option[String],
      date: Option[LocalDate]
  ): ZIO[Any, Throwable, Chunk[String]] = {
    createFileIfDoesNotExist *>
      ZStream
        .fromFile(new File(fileLocation))
        .via(ZPipeline.utf8Decode)
        .via(ZPipeline.splitLines)
        .runCollect
  }
}
