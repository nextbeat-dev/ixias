package ixias.aws.s3

import scala.util.Try

import com.amazonaws.regions.Regions
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration

import ixias.aws._

trait AmazonS3Config extends AmazonConfig {

  // --[ Properties ]-----------------------------------------------------------
  /** The keys of configuration */
  protected val CF_S3_BUCKET_NAME               = "bucket_name"
  protected val CF_S3_ENDPOINT                  = "endpoint"
  protected val CF_S3_PATH_STYLE_ACCESS_ENABLED = "path_style_access_enabled"

  // --[ Methods ]--------------------------------------------------------------

  /** Gets the AWS Bucket Name */
  protected def getBucketName(implicit dsn: DataSourceName): Try[String] =
    Try(readValue(_.get[Option[String]](CF_S3_BUCKET_NAME)).get)

  /** Gets the AWS Bucket Name */
  protected def getPathStyleAccessEnabled(implicit dsn: DataSourceName): Boolean =
    readValue(_.get[Option[Boolean]](CF_S3_PATH_STYLE_ACCESS_ENABLED)).getOrElse(true)

  /** Gets the AWS Endpoint */
  protected def getAWSEndpoint(region: Regions)(implicit dsn: DataSourceName): Option[EndpointConfiguration] =
    readValue(_.get[Option[String]](CF_S3_ENDPOINT)).map(
      new EndpointConfiguration(_, region.getName)
    )
}
