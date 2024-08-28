package com.fradulovic.cezanne.http.endpoints

import com.fradulovic.cezanne.http.req.*
import com.fradulovic.cezanne.http.resp.RegisterUserResp
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.* // type class derivation package (for jsonBody[A])

trait UserEndpoints extends BaseEndpoint {

  val registerEndpoint =
    baseEndpoint
      .tag("users")
      .name("register")
      .description("Register a user")
      .in("users")
      .post
      .in(jsonBody[RegisterUserReq].validated)
      .out(jsonBody[RegisterUserResp])

  val loginEndpoint =
    baseEndpoint
      .tag("users")
      .name("login")
      .description("Login a user")
      .in("users" / "login")
      .post
      .in(jsonBody[LoginUserReq].validated)
      .out(jsonBody[String])

  val deleteEndpoint =
    secureBasedEndpoint
      .tag("users")
      .name("delete")
      .description("Deletes user account")
      .in("users")
      .delete
      .in(jsonBody[DeleteUserReq].validated)
      .out(plainBody[String])
}
