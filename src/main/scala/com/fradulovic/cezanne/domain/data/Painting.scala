package com.fradulovic.cezanne.domain.data

import zio.json.{DeriveJsonCodec, JsonCodec}

case class Painting(
    id: Long,
    name: String,
    description: String,
    technique: String,
    images: List[String],
    price: Int,
    userId: Long
) derives JsonCodec

final case class CreatePainting(
    name: String,
    description: String,
    technique: String,
    images: List[String],
    price: Int,
    userId: Long
) {
  def toPainting(id: Long) =
    Painting(id, name, description, technique, images, price, userId)
}
