package ixias.aws.s3

import scala.util.Success

import munit.FunSuite

import com.amazonaws.regions.Regions

import ixias.aws.DataSourceName

class AmazonS3ConfigTest extends FunSuite with AmazonS3Config {

  implicit val dsn: DataSourceName = DataSourceName("aws.s3://dummy_bucket")

  // --[ Test ]-------------------------------------------------------------------
  test("AmazonS3Config#getAWSAccessKeyId") {
    assertEquals(getAWSAccessKeyId, Some("dummy"))
  }

  test("AmazonS3Config#getAWSSecretKey") {
    assertEquals(getAWSSecretKey, Some("dummy"))
  }

  test("AmazonS3Config#getAWSCredentials") {
    assertEquals(getAWSCredentials.nonEmpty, true)
  }

  test("AmazonS3Config#getAWSRegion") {
    assertEquals(getAWSRegion, Success(Regions.AP_NORTHEAST_1))
  }
}
