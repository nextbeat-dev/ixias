package ixias.security

import munit.FunSuite

class PBKDF2Test extends FunSuite {

  val password: String = "test-password"
  val hash:     String = PBKDF2.hash(password)

  test("PBKDF2 should produce a consistent hash for the same input") {
    val hash2 = PBKDF2.hash(password, PBKDF2.extractSalt(hash).get, PBKDF2.extractIterations(hash).get)
    assertEquals(hash, hash2)
  }

  test("PBKDF2 should verify the hash correctly") {
    assert(PBKDF2.compare(password, hash))
  }

  test("PBKDF2 should fail to verify an incorrect password") {
    assert(!PBKDF2.compare("wrong-password", hash))
  }

  test("PBKDF2 should extract the correct salt") {
    val salt = PBKDF2.extractSalt(hash)
    assert(salt.isDefined)
    assertEquals(salt.get.length, PBKDF2.HASH_SALT_LENGTH)
  }

  test("PBKDF2 should extract the correct salt length") {
    val saltLength = PBKDF2.extractSaltLength(hash)
    assert(saltLength.isDefined)
    assertEquals(saltLength.get, PBKDF2.HASH_SALT_LENGTH)
  }

  test("PBKDF2 should extract the correct hash") {
    val extractedHash = PBKDF2.extractHash(hash)
    assert(extractedHash.isDefined)
    assertEquals(extractedHash.get.length, PBKDF2.HASH_LENGTH)
  }

  test("PBKDF2 should extract the correct number of iterations") {
    val iterations = PBKDF2.extractIterations(hash)
    assert(iterations.isDefined)
    assert(iterations.get >= PBKDF2.HASH_ITERATIONS_MIN)
    assert(iterations.get <= PBKDF2.HASH_ITERATIONS_MAX)
  }
}
