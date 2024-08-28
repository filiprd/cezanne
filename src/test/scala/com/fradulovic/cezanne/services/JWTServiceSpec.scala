package com.fradulovic.cezanne.services

import com.fradulovic.cezanne.domain.data.User
import zio.*
import zio.test.*

object JWTServiceSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("JWTServiceTest")(
      test("create and validate jwt") {
        check(Gen.alphaNumericStringBounded(4, 8), Gen.long) { (str, id) =>
          val testEmail = s"$str@example.com"
          for {
            service   <- ZIO.service[JWTService]
            userToken <- service.createToken(User(id, testEmail, "myPassword"))
            userID    <- service.verifyToken(userToken.token)
          } yield assertTrue(userID.id == id && userID.email == testEmail)
        }
      }
    ).provide(
      JWTServiceLive.configuredLayer
    )
}
