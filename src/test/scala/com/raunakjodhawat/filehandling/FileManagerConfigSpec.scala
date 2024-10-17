package com.raunakjodhawat.filehandling

import org.junit.runner.RunWith
import zio.Scope
import zio.test.Assertion.equalTo
import zio.test._
import zio.test.junit.{JUnitRunnableSpec, ZTestJUnitRunner}
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault}

object FileManagerConfigSpec extends JUnitRunnableSpec {

  val fileLocation = "src/test/resources/todos.txt"
  val tempFileLocation = "src/test/resources/todos_temp.txt"
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("FileManagerConfig tests")(
      test("fileLocation should be set") {
        assert(FileManagerConfig.fileLocation)(
          equalTo(fileLocation)
        )
      },
      test("tempFileLocation should be set") {
        assert(FileManagerConfig.tempFileLocation)(
          equalTo(tempFileLocation)
        )
      }
    )
}
