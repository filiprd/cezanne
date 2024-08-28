package com.fradulovic.cezanne.repos

import com.fradulovic.cezanne.domain.data.Painting
import zio.*
import zio.test.*
import java.sql.SQLException

object PaintingRepoSpec extends ZIOSpecDefault with RepoSpec {

  override val initScript: String = "postgres/paintings.sql"

  private val testPainting =
    Painting(1, "Sunset", "My first painting", "Oil on canvas", List("http://images.com/73ch2"), 99, 1)

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("PaintingRepositoryTest")(
      test("create painting") {
        for {
          repo     <- ZIO.service[PaintingRepo]
          painting <- repo.create(testPainting)
        } yield assertTrue(painting == testPainting)
      },

      /** Test plan: create the same painting twice and observe an error in the
        * second response
        */
      test("get by id") {
        for {
          repo <- ZIO.service[PaintingRepo]
          _    <- repo.create(testPainting)
          res  <- repo.getById(1L)
        } yield assertTrue(res.get == testPainting)
      }
    ).provide(
      PaintingRepoLive.layer,
      dataSourceLayer,
      Repository.quilLayer,
      Scope.default
    )
}
