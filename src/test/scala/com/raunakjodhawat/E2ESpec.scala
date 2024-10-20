package com.raunakjodhawat

import com.raunakjodhawat.filehandling.FileManagerConfig
import zio.{Chunk, Scope, ULayer, ZIO, ZIOAppArgs, ZLayer}
import zio.test.Assertion.equalTo
import zio.test.TestAspect.{beforeAll, sequential}
import zio.test._
import zio.test.junit.JUnitRunnableSpec

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
        } yield assert(output)(equalTo(Vector("default\n")))
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
      test("creating profile with existing name") {
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
      },
      test("deleting the default profile") {
        for {
          _ <- Main.cliApp.run(
            List[String]("delete", "-p", "--name", "default")
          )
          output <- TestConsole.output
        } yield assert(output)(
          equalTo(
            Vector(
              "Profile 'default' can't be deleted, as it's the default\n"
            )
          )
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
              "default\nprofile1\nprofile2\n"
            )
          )
        )
      },
      test("delete 2 profiles") {
        for {
          _ <- Main.cliApp.run(
            List[String]("delete", "-p", "--name", "profile1")
          )
          _ <- Main.cliApp.run(
            List[String]("delete", "-p", "--name", "profile2")
          )
          _ <- Main.cliApp.run(List[String]("get", "-p"))
          output <- TestConsole.output
        } yield assert(output)(
          equalTo(
            Vector(
              "Profile 'profile1' deleted successfully\n",
              "Profile 'profile2' deleted successfully\n",
              "default\n"
            )
          )
        )
      },
      test("updating a profile name with non-existent profile") {
        for {
          _ <- Main.cliApp.run(
            List[String](
              "update",
              "-p",
              "--old",
              "profile1",
              "--new",
              "profile2"
            )
          )
          output <- TestConsole.output
        } yield assert(output)(
          equalTo(Vector("Profile 'profile1' does not exist\n"))
        )
      },
      test("create 1 profile") {
        for {
          _ <- Main.cliApp.run(
            List[String]("create", "-p", "--name", "profile1")
          )
          _ <- Main.cliApp.run(List[String]("get", "-p"))
          output <- TestConsole.output
        } yield assert(output)(
          equalTo(
            Vector(
              "Profile 'profile1' created successfully\n",
              "default\nprofile1\n"
            )
          )
        )
      },
      test("updating a profile name") {
        for {
          _ <- Main.cliApp.run(
            List[String](
              "update",
              "-p",
              "--old",
              "profile1",
              "--new",
              "profile2"
            )
          )
          _ <- Main.cliApp.run(List[String]("get", "-p"))
          output <- TestConsole.output
        } yield assert(output)(
          equalTo(
            Vector(
              "Profile 'profile1' updated to 'profile2'\n",
              "default\nprofile2\n"
            )
          )
        )
      },
      test("updating a profile with some existing name") {
        for {
          _ <- Main.cliApp.run(
            List[String](
              "update",
              "-p",
              "--old",
              "profile2",
              "--new",
              "profile2"
            )
          )
          output <- TestConsole.output
        } yield assert(output)(
          equalTo(
            Vector(
              "Profile 'profile2' already exists\n"
            )
          )
        )
      },
      test("updating default profile name") {
        for {
          _ <- Main.cliApp.run(
            List[String](
              "update",
              "-p",
              "--old",
              "default",
              "--new",
              "profile1"
            )
          )
          _ <- Main.cliApp.run(List[String]("get", "-p"))
          output <- TestConsole.output
        } yield assert(output)(
          equalTo(
            Vector(
              "Profile 'default' updated to 'profile1'\n",
              "profile1\nprofile2\n"
            )
          )
        )
      }
    ) @@ sequential

  def todoTests: Spec[Any with ZIOAppArgs with Scope, Any] =
    suite("todo tests")(
      test("Get all todos") {
        for {
          _ <- Main.cliApp.run(List[String]("get", "-t"))
          output <- TestConsole.output
        } yield assert(output.length)(equalTo(0))
      },
      test("create a todo for default profile") {
        for {
          _ <- Main.cliApp.run(
            List[String](
              "create",
              "push great code",
              "-d",
              "2021-10-10"
            )
          )
          output <- TestConsole.output
        } yield assert(output)(
          equalTo(Vector("Task created successfully\n"))
        )
      },
      test("creating another todo for default profile") {
        for {
          _ <- Main.cliApp.run(
            List[String](
              "create",
              "--name",
              "default",
              "--todo",
              "this is the second task",
              "-d",
              "2021-10-10"
            )
          )
          output <- TestConsole.output
        } yield assert(output)(
          equalTo(
            Vector(
              "Task created successfully\n"
            )
          )
        )

      }
//      test("deleting a todo") {
//        for {
//          _ <- Main.cliApp.run(
//            List[String]("delete", "-t", "--todo", "todo1")
//          )
//          output <- TestConsole.output
//        } yield assert(output)(
//          equalTo(Vector("Todo 'todo1' deleted successfully\n"))
//        )
//      },
//      test("deleting a non-existent todo") {
//        for {
//          _ <- Main.cliApp.run(
//            List[String]("delete", "-t", "--todo", "todo1")
//          )
//          output <- TestConsole.output
//        } yield assert(output)(
//          equalTo(Vector("Todo 'todo1' does not exist\n"))
//        )
//      },
//      test("create 2 todos and get all todos") {
//        for {
//          _ <- Main.cliApp.run(
//            List[String](
//              "create",
//              "-t",
//              "--todo",
//              "todo1",
//              "--date",
//              "2021-10-10"
//            )
//          )
//          _ <- Main.cliApp.run(
//            List[String](
//              "create",
//              "-t",
//              "--todo",
//              "todo2",
//              "--date",
//              "2021-10-10"
//            )
//          )
//          _ <- Main.cliApp.run(List[String]("get", "-t"))
//          output <- TestConsole.output
//        } yield assert(output)(
//          equalTo(
//            Vector(
//              "Todo 'todo1' created successfully\n",
//              "Todo 'todo2' created successfully\n",
//              "default\ntodo1\ntodo2\n"
//            )
//          )
//        )
//      }
    )
  override def spec: Spec[TestEnvironment with Scope, Any] = suite("E2E tests")(
    profileTests,
    todoTests
  ).provideSomeLayer[TestEnvironment with Scope](
    testArgsLayer
  ) @@ beforeAll(beforeHook)
}
