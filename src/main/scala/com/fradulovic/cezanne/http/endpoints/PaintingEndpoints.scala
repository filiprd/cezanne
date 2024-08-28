package com.fradulovic.cezanne.http.endpoints

import com.fradulovic.cezanne.domain.data.Painting
import com.fradulovic.cezanne.http.req.CreatePaintingReq
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.* // type class derivation package (for jsonBody[A])

trait PaintingEndpoints extends BaseEndpoint {
  val createEndpoint =
    secureBasedEndpoint
      .tag("paintings")
      .name("create")
      .description("Create a new painting")
      .in("paintings")
      .post
      .in(jsonBody[CreatePaintingReq])
      .out(jsonBody[Painting])

  val getAllEndpoint =
    baseEndpoint
      .tag("paintings")
      .name("getAll")
      .description("Get all paintings")
      .in("paintings")
      .get
      .out(jsonBody[List[Painting]])

  val getByIdEndpoint =
    baseEndpoint
      .tag("paintings")
      .name("getById")
      .description("Get a painting by id")
      .in("paintings" / path[String]("id"))
      .get
      .out(jsonBody[Option[Painting]])
}
