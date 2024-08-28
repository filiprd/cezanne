package com.fradulovic.cezanne.services

import com.fradulovic.cezanne.domain.data.{Painting, CreatePainting}
import com.fradulovic.cezanne.repos.PaintingRepo
import zio.*

trait PaintingService {
  def create(createPainting: CreatePainting): Task[Painting]
  def getAll(): Task[List[Painting]]
  def getById(id: Long): Task[Option[Painting]]
}

class PaintingServiceLive private (repo: PaintingRepo) extends PaintingService {
  // id will be automatically created by postgres
  override def create(createPainting: CreatePainting): Task[Painting] = repo.create(createPainting.toPainting(-1L))
  override def getAll(): Task[List[Painting]]                         = repo.getAll()
  override def getById(id: Long): Task[Option[Painting]]              = repo.getById(id)
}

object PaintingServiceLive {
  val layer: ZLayer[PaintingRepo, Nothing, PaintingServiceLive] =
    ZLayer {
      ZIO.service[PaintingRepo].map(repo => new PaintingServiceLive(repo))
    }
}
