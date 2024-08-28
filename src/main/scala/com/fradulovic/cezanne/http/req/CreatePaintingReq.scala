package com.fradulovic.cezanne.http.req

import com.fradulovic.cezanne.domain.data.CreatePainting
import zio.json.{DeriveJsonCodec, JsonCodec}

final case class CreatePaintingReq(
    name: String,
    description: String,
    technique: String,
    images: List[String],
    price: Int,
    userId: Long
) derives JsonCodec {
  def toDomain(userId: Long) =
    CreatePainting(name, description, technique, images, price, userId)
}
