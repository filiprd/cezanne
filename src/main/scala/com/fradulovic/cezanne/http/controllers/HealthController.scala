package com.fradulovic.cezanne.http.controllers

import com.fradulovic.cezanne.http.endpoints.HealthEndpoint
import zio.*

class HealthController private extends BaseController with HealthEndpoint {
  val health = healthEndpoint.serverLogicSuccess[Task](_ => ZIO.succeed("Health good"))

  override val routes = List(health)
}

object HealthController {
  val makeZIO = ZIO.succeed(new HealthController)
}
