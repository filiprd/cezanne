package com.fradulovic.cezanne.syntax

import zio.*
import zio.test.*

object AssertionSyntax {
  extension [R, E, A](zio: ZIO[R, E, A]) {
    def assert(assertion: Assertion[A]): ZIO[R, E, TestResult] =
      assertZIO(zio)(assertion)
    def assert(predicate: (=> A) => Boolean): ZIO[R, E, TestResult] =
      assert(Assertion.assertion("test assertion")(predicate))
  }
}
