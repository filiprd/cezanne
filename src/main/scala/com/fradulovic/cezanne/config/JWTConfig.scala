package com.fradulovic.cezanne.config

case class JWTConfig(
    secret: String,
    ttl: Long
)
