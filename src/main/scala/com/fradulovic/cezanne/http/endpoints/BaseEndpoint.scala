package com.fradulovic.cezanne.http.endpoints

import com.fradulovic.cezanne.domain.errors.HttpError
import sttp.tapir.*

trait BaseEndpoint {

  /** An endpoint with defined http error handling and message propagation in
    * responses
    */
  val baseEndpoint =
    endpoint
      .errorOut(statusCode and plainBody[String])
      .mapErrorOut[Throwable](HttpError.decode)(HttpError.encode)
      .prependIn("api" / "v1")

  val secureBasedEndpoint =
    baseEndpoint.securityIn(auth.bearer[String]())

  extension [A](jsonBody: EndpointIO.Body[String, A]) {
    def validated(using validator: Validator[A]) = jsonBody.validate(validator)
  }
}
