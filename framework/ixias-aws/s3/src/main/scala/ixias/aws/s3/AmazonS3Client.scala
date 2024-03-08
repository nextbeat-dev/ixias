package ixias.aws.s3

import java.io.File
import java.net.URL
import java.util.Date

import scala.util.Try

import com.amazonaws.auth.{ AWSCredentialsProvider, AWSStaticCredentialsProvider, DefaultAWSCredentialsProviderChain }
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.model._

import ixias.util.Logging
import ixias.aws._

trait AmazonS3Client extends AmazonS3Config with Logging {

  implicit val dsn: DataSourceName

  /** Gets the object stored in Amazon S3 under the specified bucket and key. */
  def load(bucketName: String, key: String): Try[S3Object] =
    action(
      _.getObject(
        new GetObjectRequest(
          bucketName,
          key
        )
      )
    )

  /** Uploads a new object to the specified Amazon S3 bucket. */
  def upload(s3Object: S3Object): Try[PutObjectResult] =
    action(
      _.putObject(
        new PutObjectRequest(
          s3Object.getBucketName,
          s3Object.getKey,
          s3Object.getObjectContent,
          s3Object.getObjectMetadata
        )
      )
    )

  /** Uploads a new object to the specified Amazon S3 bucket. */
  def upload(bucketName: String, key: String, content: File): Try[PutObjectResult] =
    action(
      _.putObject(
        new PutObjectRequest(
          bucketName,
          key,
          content
        )
      )
    )

  /** Deletes the specified object in the specified bucket. */
  def remove(bucketName: String, key: String): Try[Unit] =
    action(
      _.deleteObject(
        new DeleteObjectRequest(
          bucketName,
          key
        )
      )
    )

  /** Deletes the file object list in the specified bucket. */
  def bulkRemove(bucketName: String, keys: String*): Try[DeleteObjectsResult] =
    action(
      _.deleteObjects(
        new DeleteObjectsRequest(
          bucketName
        ).withKeys(keys: _*)
      )
    )

  /** Generate a signed URL. */
  def generatePreSignedUrl(
    bucketName: String,
    key:        String,
    method:     HttpMethod,
    expiration: Date
  ): Try[URL] = action(_.generatePresignedUrl(bucketName, key, expiration, method))

  /** Method to retrieve AmazonS3 client information so that it can be executed with any type. */
  def action[T](access: AmazonS3 => T): Try[T] = getClient.map(access)

  def actionFromTry[T](access: AmazonS3 => Try[T]): Try[T] =
    for {
      client <- getClient
      result <- access(client)
    } yield result

  def actionFromOption[T](access: AmazonS3 => Option[T]): Option[T] =
    for {
      client <- getClient.toOption
      result <- access(client)
    } yield result

  def actionFromEither[T](access: AmazonS3 => Either[Throwable, T]): Either[Throwable, T] =
    for {
      client <- getClient.toEither
      result <- access(client)
    } yield result

  private lazy val getClient: Try[AmazonS3] = {
    getAWSCredentials.fold(getClient(new DefaultAWSCredentialsProviderChain())) { credentials =>
      getClient(new AWSStaticCredentialsProvider(credentials))
    }
  }

  private def getClient(credentialsProvider: AWSCredentialsProvider): Try[AmazonS3] = {
    logger.debug("Get a AWS Client dsn=%s hash=%s".format(dsn, dsn.hashCode))
    for {
      region <- getAWSRegion
    } yield {
      val builder                = AmazonS3ClientBuilder.standard()
      val endpoint               = getAWSEndpoint(region)
      val pathStyleAccessEnabled = getPathStyleAccessEnabled
      endpoint match {
        case Some(value) =>
          builder.withEndpointConfiguration(value)
        case None =>
          builder.withRegion(region)
      }
      builder
        .withCredentials(credentialsProvider)
        .withPathStyleAccessEnabled(pathStyleAccessEnabled)
        .build()
    }
  }
}

object AmazonS3Client {
  def apply(dataSourceName: String): AmazonS3Client =
    new AmazonS3Client {
      override implicit val dsn: DataSourceName = DataSourceName(dataSourceName)
    }

  def apply(dataSourceName: DataSourceName): AmazonS3Client =
    new AmazonS3Client {
      override implicit val dsn: DataSourceName = dataSourceName
    }
}
