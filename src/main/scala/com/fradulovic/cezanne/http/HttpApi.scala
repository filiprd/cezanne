package com.fradulovic.cezanne.http

import com.fradulovic.cezanne.http.controllers.{BaseController, PaintingController, HealthController, UserController}
import com.fradulovic.cezanne.http.endpoints.BaseEndpoint
import com.fradulovic.cezanne.services.{PaintingService, JWTService, UserService}
import sttp.tapir.server.ServerEndpoint
import zio.*

object HttpApi {

  private def gatherRouts(controllers: List[BaseController]): List[ServerEndpoint[Any, Task]] =
    controllers.flatMap(_.routes)

  private def makeControllers
      : ZIO[JWTService with UserService with PaintingService, Nothing, List[BaseController with BaseEndpoint]] =
    for {
      health    <- HealthController.makeZIO
      paintings <- PaintingController.makeZIO
      users     <- UserController.makeZIO
    } yield List(health, paintings, users)

  val endpointsZIO: ZIO[JWTService with UserService with PaintingService, Nothing, List[ServerEndpoint[Any, Task]]] =
    makeControllers.map(gatherRouts)
}
