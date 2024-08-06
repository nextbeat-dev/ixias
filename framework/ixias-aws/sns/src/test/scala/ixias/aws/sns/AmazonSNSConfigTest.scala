package ixias.aws.sns

import scala.util.Success

import munit.FunSuite

import software.amazon.awssdk.regions.Region

import ixias.aws.DataSourceName

class AmazonSNSConfigTest extends FunSuite with AmazonSNSConfig {

  implicit val dsn: DataSourceName = DataSourceName("aws.sns://test_topic")

  test("Testing the AmazonSNSConfig getAWSRegion Success") {
    assertEquals(getAWSRegion, Success(Region.AP_NORTHEAST_1))
  }

  test("Testing the AmazonSNSConfig getAWSEndpoint Success") {
    assertEquals(getAWSEndpoint().nonEmpty, true)
  }

  test("Testing the AmazonSNSConfig Retrieved endpoints match those specified") {
    assertNotEquals(getAWSEndpoint().get.toString, "http://hogehoge:4566")
  }

  test("Testing the AmazonSNSConfig The retrieved endpoint does not match the specified one.") {
    assertNotEquals(getAWSEndpoint().get.toString, "http://hogehoge:9911")
  }

  test("Testing the AmazonSNSConfig getAWSEndpoint Failure") {
    intercept[IllegalArgumentException] {
      getAWSEndpoint()(DataSourceName("aws.sns"))
    }
  }

  test("Testing the AmazonSNSConfig getAWSAccessKeyId Success") {
    assertEquals(getAWSAccessKeyId, Some("dummy"))
  }

  test("Testing the AmazonSNSConfig getTopicARN Success") {
    assertEquals(getAWSSecretKey, Some("dummy"))
  }

  test("Testing the AmazonSNSConfig getAWSAccessKeyId Success") {
    assertEquals(getTopicARN, Success("arn:aws:sns:ap-northeast-1:000000000000:testSNS"))
  }
}
