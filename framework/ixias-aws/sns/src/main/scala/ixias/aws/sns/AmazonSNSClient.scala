package ixias.aws.sns

import scala.util.Try
import scala.jdk.CollectionConverters._

import com.amazonaws.auth.{AWSCredentialsProvider, AWSStaticCredentialsProvider, DefaultAWSCredentialsProviderChain}
import com.amazonaws.services.sns.model.{ PublishResult, MessageAttributeValue, PublishRequest }
import com.amazonaws.services.sns.{ AmazonSNS, AmazonSNSClientBuilder }

import ixias.util.Logging
import ixias.aws.DataSourceName

trait AmazonSNSClient extends AmazonSNSConfig with Logging {

  implicit val dsn: DataSourceName

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Sends a message to a topic's subscribed endpoints.
   *
   * example:
   * {{{
   *   val client = AmazonSNSClient("aws.sns://topic")
   *   client.publish("Test")
   * }}}
   */
  def publish(message: String): Try[PublishResult] =
    if (isSkip) {
      getTopicARN map { topic =>
        logger.info("AWS-SNS :: skip to publish a message. topic = %s, message = %s".format(topic, message))
        new PublishResult()
      }
    } else for {
      client <- getClient
      topic  <- getTopicARN
    } yield client.publish(topic, message)

  /**
   * Sends a message with subscription filter to a topic's subscribed endpoints.
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
  def publish(message: String, attributes: Map[String, MessageAttributeValue] = Map.empty): Try[PublishResult] = {
    if (isSkip) {
      getTopicARN map { topic =>
        logger.info("AWS-SNS :: skip to publish a message. topic = %s, message = %s".format(topic, message))
        new PublishResult()
      }
    } else {
      for {
        client <- getClient
        topic  <- getTopicARN
      } yield {
        val publishRequest = new PublishRequest(topic, message)
        publishRequest.setMessageAttributes(attributes.asJava)
        client.publish(publishRequest)
      }
    }
  }

  private lazy val getClient: Try[AmazonSNS] = {
    getAWSCredentials.fold(getClient(new DefaultAWSCredentialsProviderChain())) {
      credentials => getClient(new AWSStaticCredentialsProvider(credentials))
    }
  }

  private def getClient(credentialsProvider: AWSCredentialsProvider): Try[AmazonSNS] = {
    logger.debug("Get a AWS Client dsn=%s hash=%s".format(dsn, dsn.hashCode))
    for {
      region <- getAWSRegion
    } yield {
      val builder = AmazonSNSClientBuilder.standard
      val endpoint = getAWSEndpoint(region)

      if (endpoint.nonEmpty) {
        builder.withEndpointConfiguration(endpoint.get)
      } else {
        builder.withRegion(region)
      }

      builder
        .withCredentials(credentialsProvider)
        .build
    }
  }
}

object AmazonSNSClient {
  def apply(key: String): AmazonSNSClient =
    new AmazonSNSClient {
      override implicit val dsn: DataSourceName = DataSourceName(key)
    }
}
