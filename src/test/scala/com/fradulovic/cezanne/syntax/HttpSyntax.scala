package com.fradulovic.cezanne.syntax

import zio.*
import sttp.client3.*
import sttp.model.Method
import zio.json.*

object HttpSyntax {
  extension [Req](backend: SttpBackend[Task, Any]) {
    def sendReq[Resp: JsonCodec](
        method: Method,
        path: String,
        payload: Req,
        maybeToken: Option[String] = None
    )(using JsonCodec[Req]): Task[Option[Resp]] =
      basicRequest
        .method(method, uri"$path")
        .body(payload.toJson)
        .auth
        .bearer(maybeToken.getOrElse(""))
        .send(backend)
        .map(_.body)
        .map(_.toOption.flatMap(_.fromJson[Resp].toOption))

    def get[Resp: JsonCodec](path: String): Task[Option[Resp]] =
      basicRequest
        .get(uri"$path")
        .send(backend)
        .map(_.body)
        .map(_.toOption.flatMap(_.fromJson[Resp].toOption))

    def post[Resp: JsonCodec](path: String, payload: Req)(using JsonCodec[Req]): Task[Option[Resp]] =
      sendReq(Method.POST, path, payload, None)

    def postWithToken[Resp: JsonCodec](path: String, payload: Req, token: String)(using
        JsonCodec[Req]
    ): Task[Option[Resp]] =
      sendReq(Method.POST, path, payload, Some(token))

    def deleteWithToken[Resp: JsonCodec](path: String, payload: Req, token: String)(using
        JsonCodec[Req]
    ): Task[Option[Resp]] =
      sendReq(Method.DELETE, path, payload, Some(token))
  }

}
