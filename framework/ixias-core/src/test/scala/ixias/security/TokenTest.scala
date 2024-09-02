package ixias.security

import munit.FunSuite

class TokenTest extends FunSuite {

  // Concrete implementation of the Token trait for testing
  object TestToken extends Token {
    protected val generator: TokenGenerator = TokenGenerator()
  }

  test("RandomPINCode should generate a token of the correct length") {
    val length = 6
    val token  = RandomPINCode.next(length)
    assertEquals(token.length, length)
    assert(token.forall(_.isDigit))
  }

  test("RandomStringToken should generate a token of the correct length") {
    val length = 10
    val token  = RandomStringToken.next(length)
    assertEquals(token.length, length)
    assert(token.forall(_.isLetterOrDigit))
  }

  test("Token.safeEquals should return true for equal strings") {
    val str1 = "test-string"
    val str2 = "test-string"
    assert(TestToken.safeEquals(str1, str2))
  }

  test("Token.safeEquals should return false for different strings") {
    val str1 = "test-string"
    val str2 = "different-string"
    assert(!TestToken.safeEquals(str1, str2))
  }
}
