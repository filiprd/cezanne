package com.fradulovic.cezanne.http.controllers

import com.fradulovic.cezanne.crypto.Hasher.hash
import com.fradulovic.cezanne.domain.data.*
import com.fradulovic.cezanne.http.req.CreatePaintingReq
import com.fradulovic.cezanne.services.*
import com.fradulovic.cezanne.syntax.HttpSyntax.*
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import zio.*
import zio.test.*
import sttp.client3.*
import sttp.tapir.server.ServerEndpoint
import zio.json.*

object PaintingControllerSpec extends ZIOSpecDefault {

  private given zioMonadError: MonadError[Task] = new RIOMonadError[Any]

  private val testPainting =
    Painting(1, "Sunset", "My first painting", "Oil on canvas", List("http://images.com/73ch2"), 99, 1)
  private val testCreatePaintingReq =
    CreatePaintingReq("Sunset", "My first painting", "Oil on canvas", List("http://images.com/73ch2"), 99, 1)
  private val testEmail    = "user@test.com"
  private val testPassword = "myPassword"
  private val testUser     = User(1, testEmail, hash(testPassword))

  private val stubService = new PaintingService {
    override def create(createPainting: CreatePainting): Task[Painting] = ZIO.succeed(testPainting)
    override def getAll(): Task[List[Painting]]                         = ZIO.succeed(List(testPainting))
    override def getById(id: Long): Task[Option[Painting]]              = ZIO.succeed(if id == 1 then Some(testPainting) else None)
  }

  private val stubJwtLayer = ZLayer.succeed(
    new JWTService {
      override def createToken(user: User): Task[UserToken] =
        ZIO.succeed(UserToken(testUser.email, "TOKEN", Long.MaxValue))

      override def verifyToken(token: String): Task[UserID] = ZIO.succeed(UserID(testUser.id, testUser.email))
    }
  )

  private def backendStubZIO(
      endpointF: PaintingController => ServerEndpoint[Any, Task]
  ): ZIO[JWTService with PaintingService, Nothing, SttpBackend[Task, Any]] = for {
    controller <- PaintingController.makeZIO // in each test a new controller is created
    backendStub <- ZIO.succeed(
                     TapirStubInterpreter[Task, Any](SttpBackendStub(zioMonadError))
                       .whenServerEndpointRunLogic(endpointF(controller))
                       .backend()
                   )
  } yield backendStub

  /** Note: tests in suite must be separated with a "," */
  override def spec =
    suite("PaintingControllerSpec")(
      /** Test plan: create a painting and observe that the response contains
        * painting data
        */
      test("post painting") {
        for {
          backendStub   <- backendStubZIO(_.create)
          maybePainting <- backendStub.postWithToken[Painting]("/api/v1/paintings", testCreatePaintingReq, "TOKEN")
        } yield assertTrue(maybePainting.contains(testPainting))
      },

      /** Test plan: get all paintings and observe that the list contains test
        * painting
        */
      test("get all") {
        for {
          backendStub    <- backendStubZIO(_.getAll)
          maybePaintings <- backendStub.get[List[Painting]]("/api/v1/paintings")
        } yield assertTrue(maybePaintings.contains(List(testPainting)))
      },

      /** Test plan: get a painting by id and observe painting in the response
        */
      test("get by id") {
        for {
          backendStub    <- backendStubZIO(_.getById)
          maybePaintings <- backendStub.get[Painting]("/api/v1/paintings/1")
          noPaintings    <- backendStub.get[Painting]("/api/v1/paintings/123")
        } yield assertTrue(
          maybePaintings.contains(testPainting) && noPaintings.isEmpty
        )
      }
    )
      .provide(
        ZLayer.succeed(stubService),
        stubJwtLayer
      )
}
