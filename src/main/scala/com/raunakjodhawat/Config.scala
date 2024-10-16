package com.raunakjodhawat

object Config {
  val defaultUserName: String = System.getProperty("user.name")
  final case class SetDefaultUsername(username: String) extends Subcommand
}
