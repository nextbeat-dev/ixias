package ixias.aws.ses

import scala.util.Try

import software.amazon.awssdk.auth.credentials.{
  AwsCredentialsProvider,
  StaticCredentialsProvider,
  DefaultCredentialsProvider
}
import software.amazon.awssdk.services.ses.model._
import software.amazon.awssdk.services.ses.SesClient

import ixias.util.Logging
import ixias.aws._

trait AmazonSESClient extends AmazonSESConfig with Logging {

  implicit val dsn: DataSourceName

  def sendEmail(request: SendEmailRequest): Try[SendEmailResponse] =
    action(_.sendEmail(request))

  def sendTemplatedEmail(request: SendTemplatedEmailRequest): Try[SendTemplatedEmailResponse] =
    action(_.sendTemplatedEmail(request))

  def sendBounce(request: SendBounceRequest): Try[SendBounceResponse] =
    action(_.sendBounce(request))

  def action[T](access: SesClient => T): Try[T] =
    getClient.map(access)

  def actionFromTry[T](access: SesClient => Try[T]): Try[T] =
    for {
      client <- getClient
      result <- access(client)
    } yield result

  def actionFromOption[T](access: SesClient => Option[T]): Option[T] =
    for {
      client <- getClient.toOption
      result <- access(client)
    } yield result

  def actionFromEither[T](access: SesClient => Either[Throwable, T]): Either[Throwable, T] =
    for {
      client <- getClient.toEither
      result <- access(client)
    } yield result

  private lazy val getClient: Try[SesClient] = {
    getAWSCredentials.fold(getClient(DefaultCredentialsProvider.builder().build())) { credentials =>
      getClient(StaticCredentialsProvider.create(credentials))
    }
  }

  private def getClient(credentialsProvider: AwsCredentialsProvider): Try[SesClient] = {
    logger.debug("Get a AWS Client dsn=%s hash=%s".format(dsn, dsn.hashCode))
    for {
      region <- getAWSRegion
    } yield {
      val builder  = SesClient.builder()
      val endpoint = getAWSEndpoint()

      endpoint match {
        case Some(value) =>
          builder.endpointOverride(value)
        case None =>
          builder.region(region)
      }
      builder
        .credentialsProvider(credentialsProvider)
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
