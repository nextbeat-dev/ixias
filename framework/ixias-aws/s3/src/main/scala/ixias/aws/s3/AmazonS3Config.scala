package ixias.aws.s3

import scala.util.Try
import scala.concurrent.duration.Duration

import com.amazonaws.regions.Regions
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration

import ixias.aws._

trait AmazonS3Config extends AmazonConfig {

  // --[ Properties ]-----------------------------------------------------------
  /** The keys of configuration */
  protected val CF_S3_BUCKET_NAME               = "bucket_name"
  protected val CF_S3_ENDPOINT                  = "endpoint"
  protected val CF_S3_META_TABLE_NAME           = "meta_table_name"
  protected val CF_S3_PATH_STYLE_ACCESS_ENABLED = "path_style_access_enabled"
  protected val CF_S3_PRESIGNED_PUT_TIMEOUT     = "presigned_put_timeout"
  protected val CF_S3_PRESIGNED_GET_TIMEOUT     = "presigned_get_timeout"

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

  /** Gets the table name which is containing META-INFO of storage object.
    */
  def getMetaTableName(implicit dsn: DataSourceName): String =
    readValue(_.get[Option[String]](CF_S3_META_TABLE_NAME))
      .getOrElse("aws_s3_file")

  /** Gets the expiration date at which point the new pre-signed URL will no longer be accepted to get a file by Amazon
    * S3. Default timeout value : 15 mins
    */
  def getPresignedUrlTimeoutForGet(implicit dsn: DataSourceName): java.util.Date =
    new java.util.Date(
      System.currentTimeMillis() + readValue(_.get[Option[Duration]](CF_S3_PRESIGNED_GET_TIMEOUT).map(_.toMillis))
        .getOrElse(1500000L)
    )

  /** Gets the expiration date at which point the new pre-signed URL will no longer be accepted to upload a file by
    * Amazon S3. Default timeout value : 5 mins
    */
  def getPresignedUrlTimeoutForPut(implicit dsn: DataSourceName): java.util.Date =
    new java.util.Date(
      System.currentTimeMillis() + readValue(_.get[Option[Duration]](CF_S3_PRESIGNED_PUT_TIMEOUT).map(_.toMillis))
        .getOrElse(500000L)
    )
}
