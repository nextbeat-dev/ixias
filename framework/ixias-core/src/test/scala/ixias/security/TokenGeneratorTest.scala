package ixias.security

import munit.FunSuite

class TokenGeneratorTest extends FunSuite {

  test("TokenGenerator should generate a token of the correct length") {
    val length    = 10
    val generator = TokenGenerator()
    val token     = generator.next(length)
    assertEquals(token.length, length)
  }

  test("TokenGenerator should generate a token with characters from the specified table") {
    val length    = 10
    val table     = "abc123"
    val generator = TokenGenerator(table)
    val token     = generator.next(length)
    assert(token.forall(table.contains(_)))
  }
}
