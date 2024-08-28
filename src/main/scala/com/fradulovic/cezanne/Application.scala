package com.fradulovic.cezanne

import com.fradulovic.cezanne.config.{ConfigLayer, HttpConfig, JWTConfig}
import com.fradulovic.cezanne.http.HttpApi
import com.fradulovic.cezanne.repos.{PaintingRepoLive, Repository, UserRepoLive}
import com.fradulovic.cezanne.services.{PaintingServiceLive, JWTServiceLive, UserServiceLive}
import zio.*
import zio.http.{Server, ServerConfig}
import sttp.tapir.*
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}

import java.net.InetSocketAddress

object Application extends ZIOAppDefault {

  val configuredServer =
    ConfigLayer.make[HttpConfig]("cezanne.http") >>>
      ZLayer(
        ZIO.service[HttpConfig].map(config => ServerConfig.default.copy(address = InetSocketAddress(config.port)))
      ) >>> Server.live

  val server = for {
    endpoints <- HttpApi.endpointsZIO
    _ <- Server.serve(
           ZioHttpInterpreter(
             ZioHttpServerOptions.default
           ).toHttp(endpoints)
         )
    _ <- Console.printLine("Cezanne is up and running!")
  } yield ()

  def run = server.provide(
    Server.default,
    // services
    PaintingServiceLive.layer,
    UserServiceLive.layer,
    JWTServiceLive.configuredLayer,
    // repos
    PaintingRepoLive.layer,
    UserRepoLive.layer,
    // other
    Repository.dataLayer
  )
}
