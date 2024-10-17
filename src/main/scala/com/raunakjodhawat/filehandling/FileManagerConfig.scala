package com.raunakjodhawat.filehandling

import com.typesafe.config.ConfigFactory
object FileManagerConfig {
  private val configLayer = ConfigFactory.load().getConfig("fileManager")
  val fileLocation: String = configLayer.getString("fileLocation")
  val tempFileLocation: String = configLayer.getString("tempFileLocation")
}
