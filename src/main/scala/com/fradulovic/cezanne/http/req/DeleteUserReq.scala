package com.fradulovic.cezanne.http.req

import com.fradulovic.cezanne.http.req.validation.EmailValidator
import sttp.tapir.Validator
import zio.json.JsonCodec

case class DeleteUserReq(
    email: String,
    password: String
) derives JsonCodec

object DeleteUserReq {
  given validator: Validator[DeleteUserReq] = EmailValidator.derived(_.email)
}
