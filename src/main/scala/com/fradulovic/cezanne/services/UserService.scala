package com.fradulovic.cezanne.services

import com.fradulovic.cezanne.domain.data.{User, UserToken}
import com.fradulovic.cezanne.repos.UserRepo
import com.fradulovic.cezanne.crypto.Hasher.*
import com.fradulovic.cezanne.domain.errors.UnauthorizedException
import zio.*

trait UserService {
  def register(email: String, password: String): Task[User]
  def verifyPassword(email: String, password: String): Task[Boolean]
  def loginUser(email: String, password: String): Task[Option[UserToken]]
  def delete(email: String, password: String): Task[Unit]
}

class UserServiceLive private (jwtService: JWTService, repo: UserRepo) extends UserService {
  // id will be automatically created by postgres
  override def register(email: String, password: String): Task[User] = {
    val hashedPassword = hash(password)
    repo.create(User(-1L, email, hashedPassword))
  }

  override def verifyPassword(email: String, password: String): Task[Boolean] =
    for {
      maybeUser <- repo.getByEmail(email)
      isVerified = maybeUser.map(u => isValidHash(password, u.hashedPassword))
    } yield isVerified.getOrElse(false)

  override def loginUser(email: String, password: String): Task[Option[UserToken]] =
    for {
      user <- repo.getByEmail(email).someOrFail(new RuntimeException("Incorrect email or password"))
      isAuthenticated <- ZIO
                           .attempt(isValidHash(password, user.hashedPassword))
                           .catchAll(_ => ZIO.fail(new RuntimeException("Incorrect email or password")))
      jwt <- jwtService.createToken(user).when(isAuthenticated)
    } yield jwt

  override def delete(email: String, password: String): Task[Unit] =
    for {
      user            <- repo.getByEmail(email).someOrFail(UnauthorizedException)
      isAuthenticated <- ZIO.attempt(isValidHash(password, user.hashedPassword))
      _               <- if isAuthenticated then repo.delete(user.id) else ZIO.fail(UnauthorizedException)
    } yield ()
}

object UserServiceLive {
  val layer: ZLayer[UserRepo with JWTService, Nothing, UserServiceLive] = ZLayer {
    for {
      jwtService  <- ZIO.service[JWTService]
      repoService <- ZIO.service[UserRepo]
    } yield UserServiceLive(jwtService, repoService)
  }

}
