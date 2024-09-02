package ixias.security

import munit.FunSuite

import org.keyczar._

class KeyReaderTest extends FunSuite {

  test("KeyReader should return the correct key") {
    val key = new HmacKey("test-key".getBytes)
    val reader = HmacKeyReader(key)

    assertEquals(reader.getKey, "{\"size\":64,\"hmacKeyString\":\"dGVzdC1rZXk\"}")
  }

  test("KeyReader should return the correct key for a given version") {
    val key = new HmacKey("test-key".getBytes)
    val reader = HmacKeyReader(key)

    assertEquals(reader.getKey(0), "{\"size\":64,\"hmacKeyString\":\"dGVzdC1rZXk\"}")
  }

  test("KeyReader should return the correct metadata") {
    val key = new HmacKey("test-key".getBytes)
    val reader = HmacKeyReader(key)

    assertEquals(reader.getMetadata, "{\"encrypted\":false,\"purpose\":\"SIGN_AND_VERIFY\",\"versions\":[{\"exportable\":false,\"versionNumber\":0,\"status\":\"PRIMARY\"}],\"name\":\"Imported from HMAC\",\"type\":\"HMAC_SHA1\"}")
  }

  test("AesKeyReader should return the correct key") {
    val key = new AesKey("test-key".getBytes, new HmacKey("test-key".getBytes))
    val reader = AesKeyReader(key)

    assertEquals(reader.getKey, "{\"mode\":\"CBC\",\"size\":64,\"hmacKey\":{\"size\":64,\"hmacKeyString\":\"dGVzdC1rZXk\"},\"aesKeyString\":\"dGVzdC1rZXk\"}")
  }

  test("AesKeyReader should return the correct key for a given version") {
    val key = new AesKey("test-key".getBytes, new HmacKey("test-key".getBytes))
    val reader = AesKeyReader(key)

    assertEquals(reader.getKey(0), "{\"mode\":\"CBC\",\"size\":64,\"hmacKey\":{\"size\":64,\"hmacKeyString\":\"dGVzdC1rZXk\"},\"aesKeyString\":\"dGVzdC1rZXk\"}")
  }

  test("AesKeyReader should return the correct metadata") {
    val key = new AesKey("test-key".getBytes, new HmacKey("test-key".getBytes))
    val reader = AesKeyReader(key)

    assertEquals(reader.getMetadata, "{\"encrypted\":false,\"purpose\":\"DECRYPT_AND_ENCRYPT\",\"versions\":[{\"exportable\":false,\"versionNumber\":0,\"status\":\"PRIMARY\"}],\"name\":\"Imported from AES\",\"type\":\"AES\"}")
  }
}
