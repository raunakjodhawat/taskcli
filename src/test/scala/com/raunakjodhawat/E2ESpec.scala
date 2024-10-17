package com.raunakjodhawat

import com.raunakjodhawat.filehandling.FileManagerConfig
import org.junit.runner.RunWith
import zio.{Chunk, Scope, ULayer, ZIO, ZIOAppArgs, ZLayer}
import zio.test.Assertion.equalTo
import zio.test.TestAspect.{beforeAll, sequential}
import zio.test._
import zio.test.junit.{JUnitRunnableSpec, ZTestJUnitRunner}

object E2ESpec extends JUnitRunnableSpec {
  val testArgsLayer: ULayer[ZIOAppArgs] =
    ZLayer.succeed(ZIOAppArgs(Chunk.empty[String]))

  val beforeHook: ZIO[Any, Throwable, Unit] = ZIO.attempt {
    val testFile = new java.io.File(FileManagerConfig.fileLocation)
    val testTempFile = new java.io.File(FileManagerConfig.tempFileLocation)
    if (testTempFile.exists()) testTempFile.delete()
    if (testFile.exists()) testFile.delete()
  }.unit

  def profileTests: Spec[Any with ZIOAppArgs with Scope, Any] =
    suite("profile tests")(
      test("Get all profiles") {
        for {
          _ <- Main.cliApp.run(List[String]("get", "-p"))
          output <- TestConsole.output
        } yield assert(output)(equalTo(Vector("Warning! No profiles found\n")))
      },
      test("create a profile") {
        for {
          _ <- Main.cliApp.run(
            List[String]("create", "-p", "--name", "profile1")
          )
          output <- TestConsole.output
        } yield assert(output)(
          equalTo(Vector("Profile 'profile1' created successfully\n"))
        )
      },
      test("creating profile with existing profile") {
        for {
          _ <- Main.cliApp.run(
            List[String]("create", "-p", "--name", "profile1")
          )
          output <- TestConsole.output
        } yield assert(output)(
          equalTo(
            Vector(
              "Profile 'profile1' already exists\n"
            )
          )
        )

      },
      test("deleting a profile") {
        for {
          _ <- Main.cliApp.run(
            List[String]("delete", "-p", "--name", "profile1")
          )
          output <- TestConsole.output
        } yield assert(output)(
          equalTo(Vector("Profile 'profile1' deleted successfully\n"))
        )
        // todo: check if deleting adds the content to temp file
      },
      test("deleting a non-existent profile") {
        for {
          _ <- Main.cliApp.run(
            List[String]("delete", "-p", "--name", "profile1")
          )
          output <- TestConsole.output
        } yield assert(output)(
          equalTo(Vector("Profile 'profile1' does not exist\n"))
        )
      },
      test("create 2 profile and get all profile") {
        for {
          _ <- Main.cliApp.run(
            List[String]("create", "-p", "--name", "profile1")
          )
          _ <- Main.cliApp.run(
            List[String]("create", "-p", "--name", "profile2")
          )
          _ <- Main.cliApp.run(List[String]("get", "-p"))
          output <- TestConsole.output
        } yield assert(output)(
          equalTo(
            Vector(
              "Profile 'profile1' created successfully\n",
              "Profile 'profile2' created successfully\n",
              "profile1\nprofile2\n"
            )
          )
        )
      }
    ) @@ sequential
  override def spec: Spec[TestEnvironment with Scope, Any] = suite("E2E tests")(
    profileTests
  ).provideSomeLayer[TestEnvironment with Scope](
    testArgsLayer
  ) @@ beforeAll(beforeHook)
}
