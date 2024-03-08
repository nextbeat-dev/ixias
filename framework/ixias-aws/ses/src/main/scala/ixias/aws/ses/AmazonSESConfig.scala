package ixias.aws.ses

import com.amazonaws.regions.Regions
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration

import ixias.aws._

trait AmazonSESConfig extends AmazonConfig {

  // --[ Properties ]-----------------------------------------------------------
  /** The keys of configuration */
  protected val SES_ENDPOINT: String = "endpoint"

  // --[ Methods ]--------------------------------------------------------------
  /** Gets the AWS Endpoint */
  protected def getAWSEndpoint(region: Regions)(implicit dsn: DataSourceName): Option[EndpointConfiguration] =
    readValue(_.get[Option[String]](SES_ENDPOINT)).map(
      new EndpointConfiguration(_, region.getName)
    )
}
