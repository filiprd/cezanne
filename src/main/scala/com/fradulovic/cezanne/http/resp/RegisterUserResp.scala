package com.fradulovic.cezanne.http.resp

import zio.json.JsonCodec

case class RegisterUserResp(email: String) derives JsonCodec
