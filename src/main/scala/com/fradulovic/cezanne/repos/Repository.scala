package com.fradulovic.cezanne.repos

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill

object Repository {
  def quilLayer       = Quill.Postgres.fromNamingStrategy(SnakeCase)
  def dataSourceLayer = Quill.DataSource.fromPrefix("cezanne.db")

  val dataLayer = dataSourceLayer >>> quilLayer
}
