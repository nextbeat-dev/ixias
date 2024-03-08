package ixias.aws.ses

import scala.util.Try

import com.amazonaws.auth.{ AWSCredentialsProvider, AWSStaticCredentialsProvider, DefaultAWSCredentialsProviderChain }
import com.amazonaws.services.simpleemail.model._
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder

import ixias.util.Logging
import ixias.aws._

trait AmazonSESClient extends AmazonSESConfig with Logging {

  implicit val dsn: DataSourceName

  def sendEmail(request: SendEmailRequest): Try[SendEmailResult] =
    action(_.sendEmail(request))

  def sendTemplatedEmail(request: SendTemplatedEmailRequest): Try[SendTemplatedEmailResult] =
    action(_.sendTemplatedEmail(request))

  def sendBounce(request: SendBounceRequest): Try[SendBounceResult] =
    action(_.sendBounce(request))

  def action[T](access: AmazonSimpleEmailService => T): Try[T] =
    getClient.map(access)

  def actionFromTry[T](access: AmazonSimpleEmailService => Try[T]): Try[T] =
    for {
      client <- getClient
      result <- access(client)
    } yield result

  def actionFromOption[T](access: AmazonSimpleEmailService => Option[T]): Option[T] =
    for {
      client <- getClient.toOption
      result <- access(client)
    } yield result

  def actionFromEither[T](access: AmazonSimpleEmailService => Either[Throwable, T]): Either[Throwable, T] =
    for {
      client <- getClient.toEither
      result <- access(client)
    } yield result

  private lazy val getClient: Try[AmazonSimpleEmailService] = {
    getAWSCredentials.fold(getClient(new DefaultAWSCredentialsProviderChain())) { credentials =>
      getClient(new AWSStaticCredentialsProvider(credentials))
    }
  }

  private def getClient(credentialsProvider: AWSCredentialsProvider): Try[AmazonSimpleEmailService] = {
    logger.debug("Get a AWS Client dsn=%s hash=%s".format(dsn, dsn.hashCode))
    for {
      region <- getAWSRegion
    } yield {
      val builder  = AmazonSimpleEmailServiceClientBuilder.standard()
      val endpoint = getAWSEndpoint(region)

      endpoint match {
        case Some(value) =>
          builder.withEndpointConfiguration(value)
        case None =>
          builder.withRegion(region)
      }
      builder
        .withCredentials(credentialsProvider)
        .build()
    }
  }
}

object AmazonSESClient {
  def apply(dataSourceName: String): AmazonSESClient =
    new AmazonSESClient {
      override implicit val dsn: DataSourceName = DataSourceName(dataSourceName)
    }

  def apply(dataSourceName: DataSourceName): AmazonSESClient =
    new AmazonSESClient {
      override implicit val dsn: DataSourceName = dataSourceName
    }
}
