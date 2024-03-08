package ixias.aws.sns

import scala.util.Try

import com.amazonaws.regions.Regions
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration

import ixias.util.Configuration
import ixias.aws._

trait AmazonSNSConfig extends AmazonConfig {

  // --[ Properties ]-----------------------------------------------------------
  /** The keys of configuration */
  protected val CF_SNS_ENDPOINT          = "endpoint"
  protected val CF_SNS_OPT_SNS_SKIP      = "skip"
  protected val CF_SNS_OPT_SNS_TOPIC_ARN = "topic"

  // --[ Methods ]--------------------------------------------------------------
  /** Gets the AWS Endpoint
    */
  protected def getAWSEndpoint(region: Regions)(implicit dsn: DataSourceName): Option[EndpointConfiguration] =
    readValue(_.get[Option[String]](CF_SNS_ENDPOINT)).map(
      new EndpointConfiguration(_, region.getName)
    )

  /** Gets the flag to invoke SNS process.
    */
  protected def isSkip(implicit dsn: DataSourceName): Boolean =
    readValue(
      _.get[Option[Boolean]](CF_SNS_OPT_SNS_SKIP)
    ).getOrElse(false)

  /** Gets the topic ARN of Amazon SNS.
    */
  protected def getTopicARN(implicit dsn: DataSourceName): Try[String] = {
    val path = dsn.name match {
      case None       => dsn.path + "." + dsn.resource
      case Some(name) => dsn.path + "." + dsn.resource + "." + name
    }

    val section = config.get[Configuration](path).underlying

    Try(section.getString(CF_SNS_OPT_SNS_TOPIC_ARN))
  }
}
