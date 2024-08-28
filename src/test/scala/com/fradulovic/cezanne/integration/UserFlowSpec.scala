package com.fradulovic.cezanne.integration

import com.fradulovic.cezanne.config.JWTConfig
import com.fradulovic.cezanne.domain.data.User
import com.fradulovic.cezanne.http.req.{DeleteUserReq, LoginUserReq, RegisterUserReq}
import com.fradulovic.cezanne.services.{JWTService, JWTServiceLive, UserService, UserServiceLive}
import com.fradulovic.cezanne.http.controllers.UserController
import com.fradulovic.cezanne.syntax.HttpSyntax.*
import com.fradulovic.cezanne.http.resp.RegisterUserResp
import com.fradulovic.cezanne.integration.UserFlowSpec.test
import com.fradulovic.cezanne.repos.{RepoSpec, Repository, UserRepo, UserRepoLive}
import sttp.monad.MonadError
import sttp.tapir.ztapir.RIOMonadError
import sttp.tapir.server.stub.TapirStubInterpreter
import zio.*
import zio.test.*
import sttp.client3.*
import sttp.client3.testing.SttpBackendStub
import zio.json.*

object UserFlowSpec extends ZIOSpecDefault with RepoSpec {

  override val initScript: String = "postgres/integration.sql"

  private given zioMonadError: MonadError[Task] = new RIOMonadError[Any]

  private val testEmail    = "user@test.com"
  private val testPassword = "myPassword"

  private def backendStubZIO = for {
    controller <- UserController.makeZIO // in each test a new controller is created
    backendStub <- ZIO.succeed(
                     TapirStubInterpreter(SttpBackendStub(zioMonadError))
                       .whenServerEndpointsRunLogic(controller.routes)
                       .backend()
                   )
  } yield backendStub

  override def spec =
    suite("UserFlowSpec")(
      test("register user") {
        for {
          backendStub <- backendStubZIO
          maybeUserRegResp <-
            backendStub.post[RegisterUserResp]("/api/v1/users", RegisterUserReq(testEmail, testPassword))
        } yield assertTrue(
          maybeUserRegResp.contains(RegisterUserResp(testEmail))
        )
      },
      test("register user with invalid email") {
        for {
          backendStub <- backendStubZIO
          maybeUserRegResp <-
            backendStub.post[RegisterUserResp]("/api/v1/users", RegisterUserReq("invalid_email", testPassword))
        } yield assertTrue(
          maybeUserRegResp.isEmpty
        )
      },
      test("login user") {
        for {
          backendStub <- backendStubZIO
          _           <- backendStub.post[RegisterUserResp]("/api/v1/users", RegisterUserReq(testEmail, testPassword))
          maybeToken  <- backendStub.post[String]("/api/v1/users/login", LoginUserReq(testEmail, testPassword))
        } yield assertTrue(
          maybeToken.nonEmpty
        )
      },
      test("login user with invalid email") {
        for {
          backendStub <- backendStubZIO
          _           <- backendStub.post[RegisterUserResp]("/api/v1/users", RegisterUserReq(testEmail, testPassword))
          maybeToken  <- backendStub.post[String]("/api/v1/users/login", LoginUserReq("invalid_email", testPassword))
        } yield assertTrue(
          maybeToken.isEmpty
        )
      },
      test("delete user") {
        for {
          backendStub     <- backendStubZIO
          _               <- backendStub.post[RegisterUserResp]("/api/v1/users", RegisterUserReq(testEmail, testPassword))
          userRepo        <- ZIO.service[UserRepo]
          userShouldExist <- userRepo.getByEmail(testEmail)
          token <- backendStub
                     .post[String]("/api/v1/users/login", LoginUserReq(testEmail, testPassword))
                     .someOrFail(new RuntimeException("Could not login a user in test"))
          _                  <- backendStub.deleteWithToken[String]("/api/v1/users", DeleteUserReq(testEmail, testPassword), token)
          userShouldNotExist <- userRepo.getByEmail(testEmail)
        } yield assertTrue(
          userShouldExist.nonEmpty && userShouldNotExist.isEmpty
        )
      }
    )
      .provide(
        UserServiceLive.layer,
        JWTServiceLive.layer,
        UserRepoLive.layer,
        Repository.quilLayer,
        dataSourceLayer,
        ZLayer.succeed(JWTConfig("secret", 3600)),
        Scope.default
      )
}
