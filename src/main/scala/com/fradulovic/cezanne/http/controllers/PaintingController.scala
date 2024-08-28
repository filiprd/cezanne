package com.fradulovic.cezanne.http.controllers

import com.fradulovic.cezanne.domain.data.UserID
import com.fradulovic.cezanne.http.endpoints.PaintingEndpoints
import com.fradulovic.cezanne.services.{PaintingService, JWTService}
import sttp.tapir.server.ServerEndpoint
import zio.*

class PaintingController private (service: PaintingService, jwtService: JWTService)
    extends BaseController
    with PaintingEndpoints {

  val create: ServerEndpoint[Any, Task] =
    createEndpoint
      .serverSecurityLogic[UserID, Task](token => jwtService.verifyToken(token).either)
      .serverLogic { userID => req =>
        service.create(req.toDomain(userID.id)).either
      }

  val getAll: ServerEndpoint[Any, Task] =
    getAllEndpoint.serverLogic(_ => service.getAll().either)

  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint.serverLogic { id =>
      ZIO.attempt(id.toLong).flatMap(service.getById).either
    }

  override val routes = List(create, getAll, getById)
}

object PaintingController {
  val makeZIO = for {
    paintingService <- ZIO.service[PaintingService]
    jwtService      <- ZIO.service[JWTService]
  } yield new PaintingController(paintingService, jwtService)
}
