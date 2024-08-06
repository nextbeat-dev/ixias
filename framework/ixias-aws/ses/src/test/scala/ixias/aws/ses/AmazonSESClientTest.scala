package ixias.aws.ses

import munit.FunSuite

import software.amazon.awssdk.services.ses.model._

class AmazonSESClientTest extends FunSuite {

  private val sesClient: AmazonSESClient = AmazonSESClient("aws.ses://dummy")

  test("Testing the AmazonSESClient send Email request Success") {
    val body =
      Body
        .builder()
        .text(Content.builder().charset("UTF-8").data("This email was sent through Amazon SES").build())
        .build()
    val subject = Content.builder().charset("UTF-8").data("Amazon SES test (AWS SDK for Java)").build()
    val message = Message.builder().body(body).subject(subject).build()
    val request = SendEmailRequest
      .builder()
      .destination(Destination.builder().toAddresses("takahiko.tominaga@nextbeat.net").build())
      .message(message)
      .source("takahiko.tominaga@nextbeat.net")
      .build()

    assertEquals(sesClient.sendEmail(request).isSuccess, true)
  }
}
