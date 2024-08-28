package com.fradulovic.cezanne.repos

import zio.*
import io.getquill.*
import io.getquill.jdbczio.Quill
import com.fradulovic.cezanne.domain.data.Painting

trait PaintingRepo {
  def create(painting: Painting): Task[Painting]
  def update(id: Long, op: Painting => Painting): Task[Painting]
  def getById(id: Long): Task[Option[Painting]]
  def getAll(): Task[List[Painting]]
  def delete(id: Long): Task[Unit]
}

class PaintingRepoLive private (quill: Quill.Postgres[SnakeCase]) extends PaintingRepo {

  import quill.*

  inline given schema: SchemaMeta[Painting] = schemaMeta[Painting]("paintings") // table name
  // columns excluded from insert statements (will be automatically filled by postgres)
  inline given insMeta: InsertMeta[Painting] = insertMeta[Painting](_.id)
  inline given upMeta: UpdateMeta[Painting]  = updateMeta[Painting](_.id) // same for update statements

  override def create(painting: Painting): Task[Painting] =
    run {
      // lift translates data structure from scala into postgres table
      query[Painting].insertValue(lift(painting)).returning(r => r)
    }

  override def getById(id: Long): Task[Option[Painting]] =
    run {
      query[Painting].filter(_.id == lift(id))
    }.map(_.headOption)

  override def getAll(): Task[List[Painting]] =
    run(query[Painting])

  override def update(id: Long, op: Painting => Painting): Task[Painting] = for {
    current <- getById(id).someOrFail(new RuntimeException(s"Can't update painting with id $id"))
    updated <- run {
                 query[Painting].filter(_.id == lift(id)).updateValue(lift(op(current))).returning(r => r)
               }
  } yield updated

  override def delete(id: Long): Task[Unit] =
    run {
      query[Painting].filter(_.id == lift(id)).delete
    }.unit

}

object PaintingRepoLive {
  val layer: ZLayer[Quill.Postgres[SnakeCase.type], Nothing, PaintingRepoLive] =
    ZLayer {
      ZIO.service[Quill.Postgres[SnakeCase.type]].map(quill => PaintingRepoLive(quill))
    }
}
