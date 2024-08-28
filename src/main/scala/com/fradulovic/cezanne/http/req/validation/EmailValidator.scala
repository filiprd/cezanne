package com.fradulovic.cezanne.http.req.validation

import sttp.tapir.ValidationResult.{Invalid, Valid}
import sttp.tapir.Validator

object EmailValidator {
  val emailValidator =
    Validator.Custom[String](s => if s.contains('@') then Valid else Invalid("Incorrect email format"))

  def derived[A](f: A => String): Validator[A] =
    Validator.Custom[A] { a =>
      emailValidator.doValidate(f(a))
    }
}
