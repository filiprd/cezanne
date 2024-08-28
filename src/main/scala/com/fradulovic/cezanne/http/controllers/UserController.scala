package com.fradulovic.cezanne.http.controllers

import com.fradulovic.cezanne.domain.data.UserID
import com.fradulovic.cezanne.domain.errors.UnauthorizedException
import com.fradulovic.cezanne.http.endpoints.UserEndpoints
import com.fradulovic.cezanne.http.resp.RegisterUserResp
import com.fradulovic.cezanne.services.{JWTService, UserService}
import zio.*
import sttp.tapir.*
import sttp.tapir.server.*

class UserController private (userService: UserService, jwtService: JWTService)
    extends BaseController
    with UserEndpoints {

  val register: ServerEndpoint[Any, Task] =
    registerEndpoint.serverLogic { req =>
      userService
        .register(req.email, req.password)
        .map(user => RegisterUserResp(user.email))
        .either
    }

  val login: ServerEndpoint[Any, Task] =
    loginEndpoint.serverLogic { req =>
      userService
        .loginUser(req.email, req.password)
        .someOrFail(UnauthorizedException)
        .map(_.token)
        .either
    }

  val delete: ServerEndpoint[Any, Task] =
    deleteEndpoint
      .serverSecurityLogic[UserID, Task](token => jwtService.verifyToken(token).either)
      .serverLogic { userID => req =>
        if userID.email == req.email then userService.delete(req.email, req.password).map(_ => "Deleted").either
        else ZIO.fail(UnauthorizedException)
      }

  override val routes: List[ServerEndpoint[Any, Task]] =
    List(register, login, delete)
}

object UserController {
  val makeZIO = for {
    userService <- ZIO.service[UserService]
    jwtService  <- ZIO.service[JWTService]
  } yield new UserController(userService, jwtService)
}
