/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.s3.model

import ixias.aws.DataSourceName
import ixias.aws.s3.AmazonS3Config
import software.amazon.awssdk.core.Protocol
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities
import software.amazon.awssdk.services.cloudfront.internal.utils.SigningUtils.loadPrivateKey
import software.amazon.awssdk.services.cloudfront.model._
import software.amazon.awssdk.utils.DateUtils

import java.net.{URI, URL}
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions

/** The file resource definition to provide clients with a URL to display the image
  */
object UrlSigner extends AmazonS3Config {

  protected val CF_CLOUD_FRONT_KEY_PAIR_ID         = "cloudfront_key_pair_id"
  protected val CF_CLOUD_FRONT_PRIVATE_KEY_FILE    = "cloudfront_private_key_file"
  protected val CF_CLOUD_FRONT_DISTRIBUTION_DOMAIN = "cloudfront_distribution_domain"
  protected val CF_CLOUD_FRONT_SIGNED_URL_TIMEOUT  = "cloudfront_signed_url_timeout"

  /** The request to resize image.
    */
  case class Request(
    width:  Option[Int],
    height: Option[Int],
    ratio:  Option[Request.Ratio],
    format: Option[Request.Format],
    custom: Seq[(String, String)]
  ) {

    /** Generate a URL query string.
      */
    lazy val queryString = (Seq(
      width.map(v => "dw=%d".format(v)),
      height.map(v => "dh=%d".format(v)),
      ratio.map(v => "ratio=%s".format(v.value)),
      format.map(v => "fmt=%s".format(v.value))
    ).flatten ++
      custom.map(v => "%s=%s".format(v._1, v._2))).mkString("&")
  }

  /** Companion object: Resize-Request
    */
  object Request {

    // --[ Enum: Ratio ]----------------------------------------------------------
    enum Ratio(val value: String):
      case IS_1x extends Ratio(value = "1x")
      case IS_2x extends Ratio(value = "2x")
      case IS_3x extends Ratio(value = "3x")

    // --[ Enum: Format ]---------------------------------------------------------
    enum Format(val value: String):
      case IS_JPEG extends Format(value = "jpeg")
      case IS_PNG  extends Format(value = "png")
      case IS_WEBP extends Format(value = "webp")
  }

  /** Implicit convertor: To java.time.Duration
    */
  implicit def toFiniteDuration(d: FiniteDuration): java.time.Duration =
    java.time.Duration.ofNanos(d.toNanos)

  private def generateResourcePath(domain: String, key: String): String = {
    val uri = new URI(Protocol.HTTPS.toString, domain, "/" + key, null)
    uri.toASCIIString
  }

  /** Generate a signed URL that allows access to distribution and S3 objects by applying access restrictions specified
    * in a custom policy document.
    */
  def getSigendCloudFrontUrl(file: File.EmbeddedId, resize: Request)(implicit dsn: DataSourceName): URL = {
    val keyPairId  = readValue(_.get[Option[String]](CF_CLOUD_FRONT_KEY_PAIR_ID)).get
    val pkFilePath = readValue(_.get[Option[String]](CF_CLOUD_FRONT_PRIVATE_KEY_FILE)).get
    val domain     = readValue(_.get[Option[String]](CF_CLOUD_FRONT_DISTRIBUTION_DOMAIN)).get
    val timeout = readValue(_.get[Option[FiniteDuration]](CF_CLOUD_FRONT_SIGNED_URL_TIMEOUT))
      .getOrElse(FiniteDuration(30, TimeUnit.MINUTES))
    val cloudFrontUtilities = CloudFrontUtilities.create()

    // - Generate Signed-URL
    val resourcePath = generateResourcePath(domain, file.v.key)
    val cannedSignerRequest = CannedSignerRequest
      .builder()
      .resourceUrl(resourcePath + "?" + resize.queryString)
      .keyPairId(keyPairId)
      .privateKey(loadPrivateKey(new java.io.File(pkFilePath).toPath))
      .expirationDate(
        DateUtils.parseIso8601Date(
          ZonedDateTime.now.plus(timeout).toInstant.toString
        )
      )
      .build()

    new URL({
      cloudFrontUtilities.getSignedUrlWithCannedPolicy(cannedSignerRequest).url()
    })
  }
}
