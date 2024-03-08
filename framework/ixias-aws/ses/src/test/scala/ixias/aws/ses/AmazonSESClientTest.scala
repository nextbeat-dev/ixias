package ixias.aws.ses

import munit.FunSuite

import com.amazonaws.services.simpleemail.model._
import com.amazonaws.services.simpleemail.model.SendEmailRequest

class AmazonSESClientTest extends FunSuite {

  private val sesClient: AmazonSESClient = AmazonSESClient("aws.ses://dummy")

  test("Testing the AmazonSESClient send Email request Success") {
    val body =
      new Body().withText(new Content().withCharset("UTF-8").withData("This email was sent through Amazon SES"))
    val subject = new Content().withCharset("UTF-8").withData("Amazon SES test (AWS SDK for Java)")
    val message = new Message().withBody(body).withSubject(subject)
    val request = new SendEmailRequest()
      .withDestination(new Destination().withToAddresses("takahiko.tominaga@nextbeat.net"))
      .withMessage(message)
      .withSource("takahiko.tominaga@nextbeat.net")

    assertEquals(sesClient.sendEmail(request).isSuccess, true)
  }
}
