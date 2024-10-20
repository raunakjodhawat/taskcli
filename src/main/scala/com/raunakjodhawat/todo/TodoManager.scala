package com.raunakjodhawat.todo

import com.raunakjodhawat.filehandling.FileManager
import com.raunakjodhawat.filehandling.FileManagerConfig.{
  fileLocation,
  tempFileLocation
}
import zio.stream.{ZPipeline, ZStream}
import zio.{Chunk, ZIO}

import java.io.File
import java.time.LocalDate

class TodoManager(fConfig: FileManager, tempConfig: FileManager) {
  def getTodo(
      profileName: Option[String],
      date: Option[LocalDate]
  ): ZIO[Any, Throwable, Chunk[String]] = {
    fConfig.createIfDoesNotExist *>
      ZStream
        .fromFile(new File(fileLocation))
        .via(ZPipeline.utf8Decode)
        .via(ZPipeline.splitLines)
        .runCollect
  }
}
