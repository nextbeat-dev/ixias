package ixias.aws.sns

import munit.FunSuite

import software.amazon.awssdk.services.sns.model.MessageAttributeValue

class AmazonSNSClientTest extends FunSuite {

  private val client = AmazonSNSClient("aws://sns/test_topic")

  test("Testing the AmazonSNSClient publish Success") {
    assertEquals(client.publish("hogehgoe").isSuccess, true)
  }

  test("Testing the AmazonSNSClient publish with subscription filter Success") {
    val messageAttributeValue = MessageAttributeValue.builder().dataType("Number").stringValue("1").build()
    assertEquals(client.publish("hogehgoe", Map("key" -> messageAttributeValue)).isSuccess, true)
  }
}
