package com.fradulovic.cezanne.crypto

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.util.Base64

object Hasher {
  private val PBKDF2_ALG        = "PBKDF2WithHmacSHA512"
  private val PBKDF2_ITERATIONS = 1000
  private val SALT_BYTE_SIZE    = 24
  private val HASH_BYTE_SIZE    = 24
  private val skf               = SecretKeyFactory.getInstance(PBKDF2_ALG)

  private def pbkdf2(message: Array[Char], salt: Array[Byte], iterations: Int, nBytes: Int): Array[Byte] = {
    val keySpec = new PBEKeySpec(message, salt, iterations, nBytes * 8)
    skf.generateSecret(keySpec).getEncoded
  }

  private def compareBytes(a: Array[Byte], b: Array[Byte]): Boolean = {
    val range = 0 until Math.min(a.length, b.length)
    val diff = range.foldLeft(a.length ^ b.length) { case (acc, i) =>
      acc | (a(i) ^ b(i))
    }
    diff == 0
  }

  def hash(string: String): String = {
    val base64            = Base64.getEncoder
    val rng: SecureRandom = new SecureRandom()
    val salt              = Array.ofDim[Byte](SALT_BYTE_SIZE)
    rng.nextBytes(salt)
    val hashBytes = pbkdf2(string.toCharArray, salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE)
    s"$PBKDF2_ITERATIONS:${String(base64.encode(salt), "UTF-8")}:${String(base64.encode(hashBytes), "UTF-8")}"
  }

  def isValidHash(string: String, hashed: String): Boolean = {
    val base64       = Base64.getDecoder
    val hashSegments = hashed.split(':')
    val nIterations  = hashSegments(0).toInt
    val salt         = base64.decode(hashSegments(1).getBytes("UTF-8"))
    val validHash    = base64.decode(hashSegments(2).getBytes("UTF-8"))

    val testHash = pbkdf2(string.toCharArray, salt, nIterations, HASH_BYTE_SIZE)

    compareBytes(validHash, testHash)
  }
}
