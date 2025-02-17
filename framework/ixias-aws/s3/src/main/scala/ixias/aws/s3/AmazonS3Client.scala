package ixias.aws.s3

import java.io.File
import java.net.URL
import java.util.Date

import scala.util.Try

import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.auth.credentials.{
  AwsCredentialsProvider,
  StaticCredentialsProvider,
  DefaultCredentialsProvider
}
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model._
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model._

import ixias.util.Logging
import ixias.aws._

trait AmazonS3Client extends AmazonS3Config with Logging {

  implicit val dsn: DataSourceName

  /** Gets the object stored in Amazon S3 under the specified bucket and key. */
  def load(bucketName: String, key: String): Try[ResponseInputStream[GetObjectResponse]] =
    action(
      _.getObject(
        GetObjectRequest
          .builder()
          .bucket(bucketName)
          .key(key)
          .build()
      )
    )

  /** Uploads a new object to the specified Amazon S3 bucket. */
  def upload(bucketName: String, key: String, content: File): Try[PutObjectResponse] =
    action(
      _.putObject(
        PutObjectRequest
          .builder()
          .bucket(bucketName)
          .key(key)
          .build(),
        content.toPath
      )
    )

  /** Deletes the specified object in the specified bucket. */
  def remove(bucketName: String, key: String): Try[Unit] =
    action(
      _.deleteObject(
        DeleteObjectRequest
          .builder()
          .bucket(bucketName)
          .key(key)
          .build()
      )
    )

  /** Deletes the file object list in the specified bucket. */
  def bulkRemove(bucketName: String, keys: String*): Try[DeleteObjectsResponse] =
    action(
      _.deleteObjects(
        DeleteObjectsRequest
          .builder()
          .bucket(bucketName)
          .delete(
            Delete
              .builder()
              .objects(keys.map(key => ObjectIdentifier.builder().key(key).build()): _*)
              .build()
          )
          .build()
      )
    )

  /** Generate a signed URL. */
  def generateUploadPreSignedUrl(
    bucketName: String,
    key:        String,
    expiration: Date
  ): Try[URL] = {
    val duration = java.time.Duration.ofMillis(expiration.getTime - System.currentTimeMillis())
    getPersigner.map { presigner =>
      val objectRequest = PutObjectRequest
        .builder()
        .bucket(bucketName)
        .key(key)
        .build()
      val presignRequest = PutObjectPresignRequest
        .builder()
        .putObjectRequest(objectRequest)
        .signatureDuration(duration)
        .build()
      val presignedRequest = presigner.presignPutObject(presignRequest)
      presignedRequest.url()
    }
  }

  def generateGetPreSignedUrl(
    bucketName: String,
    key:        String,
    expiration: Date
  ): Try[URL] = {
    val duration = java.time.Duration.ofMillis(expiration.getTime - System.currentTimeMillis())
    getPersigner.map { presigner =>
      val objectRequest = GetObjectRequest
        .builder()
        .bucket(bucketName)
        .key(key)
        .build()
      val presignRequest = GetObjectPresignRequest
        .builder()
        .getObjectRequest(objectRequest)
        .signatureDuration(duration)
        .build()
      val presignedRequest = presigner.presignGetObject(presignRequest)
      presignedRequest.url()
    }
  }

  /** Method to retrieve AmazonS3 client information so that it can be executed with any type. */
  def action[T](access: S3Client => T): Try[T] = getClient.map(access)

  def actionFromTry[T](access: S3Client => Try[T]): Try[T] =
    for {
      client <- getClient
      result <- access(client)
    } yield result

  def actionFromOption[T](access: S3Client => Option[T]): Option[T] =
    for {
      client <- getClient.toOption
      result <- access(client)
    } yield result

  def actionFromEither[T](access: S3Client => Either[Throwable, T]): Either[Throwable, T] =
    for {
      client <- getClient.toEither
      result <- access(client)
    } yield result

  private lazy val getClient: Try[S3Client] =
    getAWSCredentials.fold(getClient(DefaultCredentialsProvider.create())) { credentials =>
      getClient(StaticCredentialsProvider.create(credentials))
    }

  private def getClient(credentialsProvider: AwsCredentialsProvider): Try[S3Client] = {
    logger.debug("Get a AWS Client dsn=%s hash=%s".format(dsn, dsn.hashCode))
    for {
      region <- getAWSRegion
    } yield {
      val builder                = S3Client.builder()
      val endpoint               = getAWSEndpoint()
      val pathStyleAccessEnabled = getPathStyleAccessEnabled
      endpoint.foreach(value => builder.endpointOverride(value))
      builder
        .credentialsProvider(credentialsProvider)
        .region(region)
        .forcePathStyle(pathStyleAccessEnabled)
        .build()
    }
  }

  private lazy val getPersigner: Try[S3Presigner] =
    getAWSCredentials.fold(getPresigner(DefaultCredentialsProvider.create())) { credentials =>
      getPresigner(StaticCredentialsProvider.create(credentials))
    }

  private def getPresigner(credentialsProvider: AwsCredentialsProvider): Try[S3Presigner] = {
    logger.debug("Get a AWS Presigner dsn=%s hash=%s".format(dsn, dsn.hashCode))
    for {
      region <- getAWSRegion
    } yield {
      val builder                = S3Presigner.builder()
      val endpoint               = getAWSEndpoint()
      val pathStyleAccessEnabled = getPathStyleAccessEnabled
      endpoint.foreach(value => builder.endpointOverride(value))
      builder
        .credentialsProvider(credentialsProvider)
        .region(region)
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
