ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.15"

lazy val root = (project in file("."))
  .settings(
    name := "taskcli"
  )

libraryDependencies += "dev.zio" %% "zio" % "2.1.11"
libraryDependencies += "dev.zio" %% "zio-cli" % "0.5.0"
libraryDependencies += "dev.zio" %% "zio-config" % "4.0.2"
libraryDependencies += "dev.zio" %% "zio-config-magnolia" % "4.0.2"
libraryDependencies += "dev.zio" %% "zio-config-typesafe" % "4.0.2"
libraryDependencies += "dev.zio" %% "zio-streams" % "2.1.11"
libraryDependencies += "dev.zio" %% "zio-test" % "2.1.11" % Test
libraryDependencies += "dev.zio" %% "zio-test-magnolia" % "2.1.11" % Test
libraryDependencies += "dev.zio" %% "zio-test-sbt" % "2.1.11" % Test
libraryDependencies += "com.github.sbt" % "junit-interface" % "0.13.3" % Test
libraryDependencies += "dev.zio" %% "zio-test-junit" % "2.1.11" % Test
testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

coverageEnabled := true
