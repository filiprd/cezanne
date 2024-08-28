package com.fradulovic.cezanne.http.req

import com.fradulovic.cezanne.http.req.validation.EmailValidator
import sttp.tapir.Validator
import zio.json.JsonCodec

case class RegisterUserReq(
    email: String,
    password: String
) derives JsonCodec

object RegisterUserReq {
  given validator: Validator[RegisterUserReq] = EmailValidator.derived(_.email)
}
