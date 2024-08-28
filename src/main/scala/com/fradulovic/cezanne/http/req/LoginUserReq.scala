package com.fradulovic.cezanne.http.req

import com.fradulovic.cezanne.http.req.validation.EmailValidator
import sttp.tapir.Validator
import zio.json.JsonCodec

case class LoginUserReq(
    email: String,
    password: String
) derives JsonCodec

object LoginUserReq {
  given validator: Validator[LoginUserReq] = EmailValidator.derived(_.email)
}
