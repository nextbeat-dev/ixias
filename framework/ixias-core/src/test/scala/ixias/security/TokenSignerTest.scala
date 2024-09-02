package ixias.security

import munit.FunSuite

import org.keyczar.HmacKey

import org.apache.commons.codec.digest.DigestUtils

class TokenSignerTest extends FunSuite {

  val secret = "test-secret"
  val key    = new HmacKey(DigestUtils.sha256(secret))
  val reader:      KeyReader   = HmacKeyReader(key)
  val tokenSigner: TokenSigner = TokenSigner(reader)

  test("TokenSigner should sign the token correctly") {
    val token       = "test-token"
    val signedToken = tokenSigner.sign(token)

    assertEquals(signedToken, "00ffabf706970210b0ff9456ed8f7c8ccf6efefb7972cd5907test-token")
  }

  test("TokenSigner should verify the signed token correctly") {
    val token       = "test-token"
    val signedToken = tokenSigner.sign(token)
    val result      = tokenSigner.verify(signedToken)

    assert(result.isSuccess)
    assertEquals(result.get, token)
  }

  test("TokenSigner should fail to verify an invalid token") {
    val invalidSignedToken = "invalid-signaturetest-token"
    val result             = tokenSigner.verify(invalidSignedToken)
    assert(result.isFailure)
  }
}
