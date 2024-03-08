package ixias.aws.s3

import java.io.File
import java.nio.file.Paths
import java.util.Date

import munit.FunSuite

import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.model._

import ixias.aws.s3.AmazonS3Client

class AmazonS3ClientTest extends FunSuite {

  private val path = Paths.get("")
  private val currentPath = path.toAbsolutePath

  private val s3Client = AmazonS3Client("aws.s3://dummy_bucket")

  private val file = new File(s"$currentPath/framework/ixias-aws/s3/src/test/resources/file/Test.txt")

  test("AmazonS3Client upload object Success") {
    assertEquals(s3Client.upload("dummy", "test", file).isSuccess, true)
  }

  test("AmazonS3Client upload object Failure") {
    assertEquals(s3Client.upload("hogehoge", "test", file).isSuccess, true)
  }

  test("AmazonS3Client generatePreSignedUrl object Success") {
    val url = s3Client.generatePreSignedUrl("dummy", "test", HttpMethod.GET, new Date(System.currentTimeMillis() + 100000L))
    assertEquals(url.isSuccess, true)
  }

  test("AmazonS3Client load object Success") {
    assertEquals(s3Client.load("dummy", "test").isSuccess, true)
  }

  test("AmazonS3Client load object Failure") {
    assertEquals(s3Client.load("dummy", "hogehoge").isFailure, true)
  }

  test("AmazonS3Client remove object Success") {
    assertEquals(s3Client.remove("dummy", "test").isSuccess, true)
  }
}
