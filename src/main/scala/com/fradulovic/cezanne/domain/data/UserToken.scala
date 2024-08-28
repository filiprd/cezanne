package com.fradulovic.cezanne.domain.data

final case class UserToken(
    email: String,
    token: String,
    expires: Long
)
