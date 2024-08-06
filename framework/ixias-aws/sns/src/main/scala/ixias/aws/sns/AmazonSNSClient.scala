package ixias.aws.sns

import scala.util.Try
import scala.jdk.CollectionConverters._

import software.amazon.awssdk.auth.credentials.{ AwsCredentialsProvider, StaticCredentialsProvider, DefaultCredentialsProvider }
import software.amazon.awssdk.services.sns.model._
import software.amazon.awssdk.services.sns.SnsClient

import ixias.util.Logging
import ixias.aws.DataSourceName

trait AmazonSNSClient extends AmazonSNSConfig with Logging {

  implicit val dsn: DataSourceName

  // --[ Methods ]--------------------------------------------------------------
  /** Sends a message to a topic's subscribed endpoints.
    *
    * example:
    * {{{
    *   val client = AmazonSNSClient("aws.sns://topic")
    *   client.publish("Test")
    * }}}
    */
  def publish(message: String): Try[PublishResponse] =
    if (isSkip) {
      getTopicARN map { topic =>
        logger.info("AWS-SNS :: skip to publish a message. topic = %s, message = %s".format(topic, message))
        PublishResponse.builder().build()
      }
    } else
      for {
        client <- getClient
        topic  <- getTopicARN
      } yield client.publish(PublishRequest.builder().topicArn(topic).message(message).build())

  /** Sends a message with subscription filter to a topic's subscribed endpoints.
    *
    * example:
    * {{{
    *   val client = AmazonSNSClient("aws.sns://topic")
    *   val messageAttributeValue = new MessageAttributeValue()
    *   messageAttributeValue.setDataType("Number")
    *   messageAttributeValue.setStringValue("1")
    *
    *   client.publish("Test", Map("filterType" -> messageAttributeValue))
    * }}}
    */
  def publish(message: String, attributes: Map[String, MessageAttributeValue] = Map.empty): Try[PublishResponse] = {
    if (isSkip) {
      getTopicARN map { topic =>
        logger.info("AWS-SNS :: skip to publish a message. topic = %s, message = %s".format(topic, message))
        PublishResponse.builder().build()
      }
    } else {
      for {
        client <- getClient
        topic  <- getTopicARN
      } yield {
        val publishRequest = PublishRequest
          .builder
          .topicArn(topic)
          .message(message)
          .messageAttributes(attributes.asJava)
          .build()
        client.publish(publishRequest)
      }
    }
  }

  private lazy val getClient: Try[SnsClient] = {
    getAWSCredentials.fold(getClient(DefaultCredentialsProvider.builder().build())) { credentials =>
      getClient(StaticCredentialsProvider.create(credentials))
    }
  }

  private def getClient(credentialsProvider: AwsCredentialsProvider): Try[SnsClient] = {
    logger.debug("Get a AWS Client dsn=%s hash=%s".format(dsn, dsn.hashCode))
    for {
      region <- getAWSRegion
    } yield {
      val builder  = SnsClient.builder()
      val endpoint = getAWSEndpoint()

      endpoint match {
        case Some(value) =>
          builder.endpointOverride(value)
        case None =>
          builder.region(region)
      }

      builder
        .credentialsProvider(credentialsProvider)
        .build
    }
  }
}

object AmazonSNSClient {
  def apply(key: String): AmazonSNSClient =
    new AmazonSNSClient {
      override implicit val dsn: DataSourceName = DataSourceName(key)
    }

  def apply(dataSourceName: DataSourceName): AmazonSNSClient =
    new AmazonSNSClient {
      override implicit val dsn: DataSourceName = dataSourceName
    }
}
