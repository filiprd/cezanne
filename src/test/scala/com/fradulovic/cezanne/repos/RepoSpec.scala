package com.fradulovic.cezanne.repos

import org.testcontainers.containers.PostgreSQLContainer
import org.postgresql.ds.PGSimpleDataSource
import javax.sql.DataSource
import zio.*

/** Test containers */
trait RepoSpec {

  val initScript: String

  /** Spawns a test Postgres instance on Docker container */
  def createContainer(): PostgreSQLContainer[Nothing] = {
    val container: PostgreSQLContainer[Nothing] =
      PostgreSQLContainer("postgres").withInitScript(initScript)
    container.start()
    container
  }

  /** Creates a DataSource which connects to Postgres instance on Docker */
  def createDataSource(container: PostgreSQLContainer[Nothing]): DataSource = {
    val dataSource = new PGSimpleDataSource()
    dataSource.setUrl(container.getJdbcUrl)
    dataSource.setUser(container.getUsername)
    dataSource.setPassword(container.getPassword)
    dataSource
  }

  /** Uses DataSource to build Quill instance */
  val dataSourceLayer: ZLayer[Any with Scope, Throwable, DataSource] =
    ZLayer {
      for {
        container <-
          ZIO.acquireRelease(ZIO.attempt(createContainer()))(container => ZIO.attempt(container.stop()).ignoreLogged)
        dataSource <- ZIO.attempt(createDataSource(container))
      } yield dataSource
    }
}
