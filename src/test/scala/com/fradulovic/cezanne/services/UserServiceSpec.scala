package com.fradulovic.cezanne.services

import com.fradulovic.cezanne.domain.data.*
import com.fradulovic.cezanne.repos.UserRepo
import com.fradulovic.cezanne.crypto.Hasher.*
import zio.*
import zio.test.*

object UserServiceSpec extends ZIOSpecDefault {

  private val testEmail    = "user@test.com"
  private val testPassword = "myPassword"
  private val testUser     = User(1, testEmail, hash(testPassword))

  private val stubRepoLayer = ZLayer.succeed(
    new UserRepo {
      override def create(user: User): Task[User]        = ZIO.succeed(testUser)
      override def getById(id: Long): Task[Option[User]] = ZIO.succeed(if id == 1L then Some(testUser) else None)
      override def getByEmail(email: String): Task[Option[User]] =
        ZIO.succeed(if email == testEmail then Some(testUser) else None)
      override def delete(id: Long): Task[Unit] = ZIO.unit
    }
  )

  private val stubJwtLayer = ZLayer.succeed(
    new JWTService {
      override def createToken(user: User): Task[UserToken] =
        ZIO.succeed(UserToken(testUser.email, "TOKEN", Long.MaxValue))
      override def verifyToken(token: String): Task[UserID] = ZIO.succeed(UserID(testUser.id, testUser.email))
    }
  )

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("UserServiceTest")(
      test("register") {
        for {
          userService <- ZIO.service[UserService]
          user        <- userService.register(testEmail, testPassword)
        } yield assertTrue(user == testUser)
      },
      test("verify password") {
        for {
          userService <- ZIO.service[UserService]
          isVerified  <- userService.verifyPassword(testEmail, testPassword)
        } yield assertTrue(isVerified)
      },
      test("do not verify password") {
        for {
          userService <- ZIO.service[UserService]
          isVerified  <- userService.verifyPassword(testEmail, "wrongPassword")
        } yield assertTrue(!isVerified)
      },
      test("login") {
        for {
          userService <- ZIO.service[UserService]
          token       <- userService.loginUser(testEmail, testPassword)
        } yield assertTrue(token.nonEmpty && token.get.email == testEmail)
      },
      test("delete with wrong credentials") {
        for {
          userService <- ZIO.service[UserService]
          err         <- userService.delete(testEmail, "wrongPass").flip
        } yield assertTrue(err.isInstanceOf[RuntimeException])
      }
    ).provide(
      UserServiceLive.layer,
      stubJwtLayer,
      stubRepoLayer
    ) // layers are not shared between tests
}
