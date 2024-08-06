package ixias.aws

import scala.util.Try

import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.auth.credentials.{ AwsCredentials, AwsBasicCredentials }

import ixias.util.Configuration

private[ixias] trait AmazonConfig {

  // --[ Properties ]-----------------------------------------------------------
  /** The keys of configuration */
  protected val CF_ACCESS_KEY = "access_key_id"
  protected val CF_SECRET_KEY = "secret_access_key"
  protected val CF_REGION     = "region"

  /** The configuration */
  protected val config: Configuration = Configuration()

  // --[ Methods ]--------------------------------------------------------------
  /** Gets the AWS access key ID for this credentials object.
    */
  protected def getAWSAccessKeyId(implicit dsn: DataSourceName): Option[String] =
    readValue(_.get[Option[String]](CF_ACCESS_KEY))

  /** Gets the AWS secret access key for this credentials object.
    */
  protected def getAWSSecretKey(implicit dsn: DataSourceName): Option[String] =
    readValue(_.get[Option[String]](CF_SECRET_KEY))

  /** Gets the AWS credentials object.
    */
  protected def getAWSCredentials(implicit dsn: DataSourceName): Option[AwsCredentials] =
    for {
      accessKey <- getAWSAccessKeyId
      secretKey <- getAWSSecretKey
    } yield AwsBasicCredentials.builder()
      .accessKeyId(accessKey)
      .secretAccessKey(secretKey)
      .build()

  /** Gets a region enum corresponding to the given region name.
    */
  protected def getAWSRegion(implicit dsn: DataSourceName): Try[Region] =
    Try(Region.of(readValue(_.get[Option[String]](CF_REGION)).get))

  /** Get a value by specified key.
    */
  protected def readValue[A](f: Configuration => Option[A])(implicit dsn: DataSourceName): Option[A] =
    (Seq(
      dsn.name.map(name => dsn.path + "." + dsn.resource + "." + name),
      dsn.path.split('.').headOption
    ).flatten ++ Seq(
      dsn.path + "." + dsn.resource,
      dsn.path
    )).foldLeft[Option[A]](None) {
      case (prev, path) => prev.orElse(f(config.get[Configuration](path)))
    }
}
