package com.raunakjodhawat.filehandling

import com.typesafe.config.ConfigFactory
object FileManagerConfig {
  private val configLayer = ConfigFactory.load().getConfig("fileManager")
  val fileLocation = configLayer.getString("fileLocation")
  val tempFileLocation = configLayer.getString("tempFileLocation")
}
