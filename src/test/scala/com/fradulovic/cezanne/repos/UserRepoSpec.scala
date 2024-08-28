package com.fradulovic.cezanne.repos

import com.fradulovic.cezanne.domain.data.User
import zio.*
import zio.test.*
import java.sql.SQLException

object UserRepoSpec extends ZIOSpecDefault with RepoSpec {

  override val initScript: String = "postgres/users.sql"

  private val testUser = User(1, "user@test.com", "myHashedPassword")

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("UserRepositoryTest")(
      test("create a user") {
        for {
          repo <- ZIO.service[UserRepo]
          user <- repo.create(testUser)
        } yield assertTrue(user == testUser)
      },

      /** Test plan: create the same user twice and observe an error in the
        * second response
        */
      test("creating a duplicate should error") {
        for {
          repo <- ZIO.service[UserRepo]
          _    <- repo.create(testUser)
          err  <- repo.create(testUser).flip
        } yield assertTrue(err.isInstanceOf[SQLException])
      }
    ).provide(
      UserRepoLive.layer,
      dataSourceLayer,
      Repository.quilLayer,
      Scope.default
    )
}
