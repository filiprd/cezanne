package com.fradulovic.cezanne.services

import com.fradulovic.cezanne.domain.data.*
import com.fradulovic.cezanne.repos.PaintingRepo
import zio.*
import zio.test.*
import com.fradulovic.cezanne.syntax.AssertionSyntax.*

import scala.collection.mutable

object PaintingServiceSpec extends ZIOSpecDefault {

  val createPaintingGen: Gen[Any, CreatePainting] = for {
    id          <- Gen.long
    name        <- Gen.alphaNumericStringBounded(4, 8)
    description <- Gen.alphaNumericStringBounded(4, 8)
    technique   <- Gen.alphaNumericStringBounded(4, 8)
    images      <- Gen.listOfBounded(1, 3)(Gen.alphaNumericString)
    price       <- Gen.int(1, 10000)
    userId      <- Gen.long(1L, 9999L)
  } yield CreatePainting(name, description, technique, images, price, userId)

  // need this because PostgresPaintingService can't be created with effectfull constructor
  // another way around this is in PaintingRepoSpec
  private val paintingService = ZIO.serviceWithZIO[PaintingService]

  private val stubRepoLayer = ZLayer.succeed(
    new PaintingRepo {
      // mutable reference is just one approach for defining stub
      val db: mutable.Map[Long, Painting] = mutable.Map()

      override def create(painting: Painting): Task[Painting] =
        ZIO.succeed {
          val newId       = db.keys.maxOption.getOrElse(0L) + 1
          val newPainting = painting.copy(id = newId)
          db += (newId -> newPainting)
          newPainting
        }

      override def update(id: Long, op: Painting => Painting): Task[Painting] =
        ZIO.attempt {
          val painting = db(id)
          db += (id -> op(painting))
          painting
        }

      override def delete(id: Long): Task[Unit] =
        ZIO.attempt {
          val painting = db(id)
          db -= id
        }

      override def getById(id: Long): Task[Option[Painting]] =
        ZIO.succeed(db.get(id))

      override def getAll(): Task[List[Painting]] =
        ZIO.succeed(db.values.toList)
    }
  )

  private def toCreatePaintingReq(testPainting: Painting) =
    CreatePainting(
      testPainting.name,
      testPainting.description,
      testPainting.technique,
      testPainting.images,
      testPainting.price,
      testPainting.userId
    )

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("PaintingServiceTest")(
      test("create") {
        check(createPaintingGen) { testCreatePainting =>
          val paintingZIO: ZIO[PaintingService, Throwable, Painting] = paintingService(_.create(testCreatePainting))
          paintingZIO.assert { painting =>
            painting == testCreatePainting.toPainting(painting.id) // adjusting painting id for db serial id
          }
        }
      },
      test("get by id") {
        check(createPaintingGen) { testCreatePainting =>
          for {
            painting    <- paintingService(_.create(testCreatePainting))
            paintingOpt <- paintingService(_.getById(painting.id))
          } yield assertTrue(paintingOpt.get == painting)
        }
      },
      test("get all") {
        val testCreatePainting =
          CreatePainting("Sunset", "My first painting", "Oil on canvas", List("http://images.com/73ch2"), 99, 1)
        for {
          painting1 <- paintingService(_.create(testCreatePainting))
          painting2 <- paintingService(_.create(testCreatePainting.copy(name = "High castle")))
          paintings <- paintingService(_.getAll())
        } yield assertTrue(paintings.toSet == Set(painting1, painting2))
      }
    ).provide(PaintingServiceLive.layer, stubRepoLayer) // layers are not shared between tests
}
