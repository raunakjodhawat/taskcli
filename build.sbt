ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.15"

lazy val root = (project in file("."))
  .settings(
    name := "taskcli"
  )

libraryDependencies += "dev.zio" %% "zio" % "2.1.11"
libraryDependencies += "dev.zio" %% "zio-cli" % "0.5.0"
