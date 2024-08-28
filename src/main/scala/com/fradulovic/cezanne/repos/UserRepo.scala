package com.fradulovic.cezanne.repos

import com.fradulovic.cezanne.domain.data.User
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

trait UserRepo {
  def create(user: User): Task[User]
  def getById(id: Long): Task[Option[User]]
  def getByEmail(email: String): Task[Option[User]]
  def delete(id: Long): Task[Unit]
}

class UserRepoLive private (quill: Quill.Postgres[SnakeCase]) extends UserRepo {

  import quill.*

  inline given schema: SchemaMeta[User] = schemaMeta[User]("users") // table name
  // columns excluded from insert statements (will be automaticlly filled by postgres)
  inline given insMeta: InsertMeta[User] = insertMeta[User](_.id)

  override def create(user: User): Task[User] =
    run {
      query[User].insertValue(lift(user)).returning(r => r)
    }

  override def getById(id: Long): Task[Option[User]] =
    run {
      query[User].filter(_.id == lift(id))
    }.map(_.headOption)

  override def getByEmail(email: String): Task[Option[User]] =
    run {
      query[User].filter(_.email == lift(email))
    }.map(_.headOption)

  override def delete(id: Long): Task[Unit] =
    run {
      query[User].filter(_.id == lift(id)).delete
    }.unit
}

object UserRepoLive {
  val layer: ZLayer[Quill.Postgres[SnakeCase.type], Nothing, UserRepoLive] =
    ZLayer {
      ZIO.service[Quill.Postgres[SnakeCase.type]].map(quill => UserRepoLive(quill))
    }
}
