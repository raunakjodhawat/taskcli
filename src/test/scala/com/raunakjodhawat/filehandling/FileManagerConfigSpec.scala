package com.raunakjodhawat.filehandling

import zio.Scope
import zio.test.Assertion.equalTo
import zio.test._
import zio.test.junit.JUnitRunnableSpec
import zio.test.{Spec, TestEnvironment}

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
