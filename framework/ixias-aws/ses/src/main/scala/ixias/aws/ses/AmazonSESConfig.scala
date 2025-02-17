package ixias.aws.ses

import java.net.URI

import ixias.aws._

trait AmazonSESConfig extends AmazonConfig {

  // --[ Properties ]-----------------------------------------------------------
  /** The keys of configuration */
  protected val SES_ENDPOINT: String = "endpoint"

  // --[ Methods ]--------------------------------------------------------------
  /** Gets the AWS Endpoint */
  protected def getAWSEndpoint()(implicit dsn: DataSourceName): Option[URI] =
    readValue(_.get[Option[String]](SES_ENDPOINT)).map(URI.create)
}
