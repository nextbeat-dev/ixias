/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.s3.model

import java.time.LocalDateTime
import java.time.temporal.ChronoField._

import scala.util.Try

import software.amazon.awssdk.services.s3.model._

import ixias.model._
import ixias.aws.DataSourceName
import ixias.aws.s3.AmazonS3Config

// The file representation for Amazon S3.
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
case class File(
  id:           Option[File.Id], // Id
  region:       String, // AWS region
  bucket:       String, // The bucket of S3
  key:          String, // The file key.
  typedef:      String, // The file-type.
  imageSize:    Option[File.ImageSize], // If file-type is image. image size is setted.
  presignedUrl: Option[java.net.URL] = None, // The presigned Url to accessing on Image
  updatedAt:    LocalDateTime        = NOW,  // The Datetime when a data was updated.
  createdAt:    LocalDateTime        = NOW   // The Datetime when a data was created.
) extends EntityModel[File.Id] {

  lazy val httpsUrl = s"${ Protocol.HTTPS.toString }://${ httpsUrn }"
  lazy val httpsUrn = presignedUrl match {
    case None => s"cdn-${ bucket }/${ key }?d=${ (updatedAt.get(MILLI_OF_SECOND) / 1000).toHexString }"
    case Some(url) =>
      s"cdn-${ bucket }/${ key }?d=${ (updatedAt.get(MILLI_OF_SECOND) / 1000).toHexString }&${ url.getQuery }"
  }
  lazy val httpsUrlOrigin = s"${ Protocol.HTTPS.toString }://${ httpsUrnOrigin }"
  lazy val httpsUrnOrigin = presignedUrl match {
    case None      => s"s3-${ region }.amazonaws.com/${ bucket }/${ key }"
    case Some(url) => url.toString.drop(url.getProtocol.length + 3)
  }

  /** Build a empty S3 object. */
  def emptyS3Object: S3Object = S3Object.builder().key(key).build()
}

// The companion object
//~~~~~~~~~~~~~~~~~~~~~~
object File extends AmazonS3Config {

  // --[ File ID ]--------------------------------------------------------------
  val Id = the[Identity[Id]]
  type Id         = Long @@ File
  type WithNoId   = Entity.WithNoId[Id, File]
  type EmbeddedId = Entity.EmbeddedId[Id, File]

  // --[ Create a new object ]--------------------------------------------------
  def apply(key: String, typedef: String, size: Option[ImageSize])(implicit
    dns: DataSourceName
  ): Try[Entity.WithNoId[File.Id, File]] =
    for {
      region <- getAWSRegion
      bucket <- getBucketName
    } yield Entity.WithNoId[File.Id, File](
      new File(None, region.id(), bucket, key, typedef, size)
    )

  // --[ The iamage size ]------------------------------------------------------
  case class ImageSize(width: Int, height: Int) {

    /** The aspect ration */
    val aspectRatio: Float = (BigDecimal(width) / BigDecimal(height))
      .setScale(4, scala.math.BigDecimal.RoundingMode.HALF_UP)
      .toFloat

    /** Change image size with keep aspect ratio. */
    def changeScale(rate: Float): ImageSize =
      this.copy(
        width  = (width.toFloat * rate).toInt,
        height = (height.toFloat * rate).toInt
      )

    /** Change image width size with keep aspect ratio. */
    def changeWidthSize(width: Int): ImageSize = {
      val rate = (BigDecimal(width) / BigDecimal(this.width))
        .setScale(4, scala.math.BigDecimal.RoundingMode.HALF_UP)
        .toFloat
      this.copy(width = width, height = (height.toFloat * rate).toInt)
    }

    /** Change image height size with keep aspect ratio. */
    def changeHeightSize(height: Int): ImageSize = {
      val rate = (BigDecimal(height) / BigDecimal(this.height))
        .setScale(4, scala.math.BigDecimal.RoundingMode.HALF_UP)
        .toFloat
      this.copy(width = (width.toFloat * rate).toInt, height = height)
    }
  }
}
