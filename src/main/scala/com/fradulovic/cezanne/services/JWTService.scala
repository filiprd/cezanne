package com.fradulovic.cezanne.services

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier.BaseVerification
import com.auth0.jwt.algorithms.Algorithm
import com.fradulovic.cezanne.config.{ConfigLayer, JWTConfig}
import com.fradulovic.cezanne.domain.data.*
import zio.*

trait JWTService {
  def createToken(user: User): Task[UserToken]
  def verifyToken(token: String): Task[UserID]
}

class JWTServiceLive private (jwtConfig: JWTConfig, clock: java.time.Clock) extends JWTService {

  private val ISSUER         = "cezanne.fradulovic.com"
  private val CLAIM_USERNAME = "username"

  private val algorithm = Algorithm.HMAC512(jwtConfig.secret)
  private val verifier =
    JWT
      .require(algorithm)
      .withIssuer(ISSUER)
      .asInstanceOf[BaseVerification]
      .build(clock)

  override def createToken(user: User): Task[UserToken] =
    for {
      now        <- ZIO.succeed(clock.instant())
      expiration <- ZIO.succeed(now.plusSeconds(jwtConfig.ttl))
      token <- ZIO.attempt(
                 JWT.create
                   .withIssuer(ISSUER)
                   .withIssuedAt(now)
                   .withExpiresAt(expiration)
                   .withSubject(user.id.toString)
                   .withClaim(CLAIM_USERNAME, user.email)
                   .sign(algorithm)
               )
    } yield UserToken(user.email, token, expiration.getEpochSecond)

  override def verifyToken(token: String): Task[UserID] =
    for {
      decoded <- ZIO.attempt(verifier.verify(token))
      userId <- ZIO.attempt(
                  UserID(decoded.getSubject.toLong, decoded.getClaim(CLAIM_USERNAME).asString)
                )
    } yield userId
}

object JWTServiceLive {
  val layer: ZLayer[JWTConfig, Nothing, JWTServiceLive] =
    ZLayer {
      for {
        jwtConfig <- ZIO.service[JWTConfig]
        clock     <- Clock.javaClock
      } yield JWTServiceLive(jwtConfig, clock)
    }

  val configuredLayer: ZLayer[Any, Throwable, JWTServiceLive] =
    ConfigLayer.make[JWTConfig]("cezanne.jwt") >>> layer
}
