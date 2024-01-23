/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.s3

import scala.concurrent.Future

import com.amazonaws.services.s3.model.S3ObjectInputStream

import ixias.aws.s3.model.File
import ixias.aws.s3.persistence.SlickResource
import ixias.aws.s3.backend.AmazonS3Backend

import ixias.slick.jdbc.MySQLProfile.api._

// S3 management repository
//~~~~~~~~~~~~~~~~~~~~~~~~~~~
trait AmazonS3Repository extends AmazonS3Backend with SlickResource {

  def master: Database
  def slave: Database

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Get file object.
   */
  def get(id: File.Id): Future[Option[EntityEmbeddedId]] =
    slave.run[Option[File]](fileTable.filter(_.id === id).result.headOption)

  /**
   * Get file object with a pre-signed URL for accessing an Amazon S3 resource.
   */
  def getWithPresigned(id: File.Id): Future[Option[EntityEmbeddedId]] =
    get(id) flatMap {
      case None       => Future.successful(None)
      case Some(file) => for {
        client <- getClient
        url    <- client.genPresignedUrlForAccess(file.v)
      } yield Some(file.map(_.copy(presignedUrl = Some(url))))
    }

  /**
   * Get file object as `Entity` with a input stream for it.
   */
  def getWithContent(id: File.Id): Future[Option[(EntityEmbeddedId, S3ObjectInputStream)]] =
    get(id) flatMap {
      case None       => Future.successful(None)
      case Some(file) => for {
        client   <- getClient
        s3object <- client.load(file.v)
      } yield Some((file, s3object.getObjectContent))
    }

  /**
   * Finds file objects by set of file ids.
   */
  def filter(ids: Seq[File.Id]): Future[Seq[EntityEmbeddedId]] =
    slave.run[Seq[File]](fileTable.filter(_.id inSet ids).result)

  /**
   * Finds file objects with a pre-signed URL by set of file ids.
   */
  def filterWithPresigned(ids: Seq[File.Id]): Future[Seq[EntityEmbeddedId]] =
    for {
      client   <- getClient
      fileSeq1 <- filter(ids)
      fileSeq2 <- Future.sequence(fileSeq1.map {
        file => for {
          url <- client.genPresignedUrlForAccess(file.v)
        } yield file.v.copy(presignedUrl = Some(url))
      })
    } yield fileSeq2

  /**
   * Save the file information.
   * At the same time upload a specified file to S3.
   */
  def add(file: File#WithNoId, content: java.io.File): Future[File.Id] =
    for {
      client    <- getClient
      _         <- client.upload(file.v, content)
      Some(fid) <- master.run(
        fileTable returning fileTable.map(_.id) += file.v
      )
    } yield File.Id(fid)

  /**
   * Save the file information.
   * However, the file body content has not yet been uploaded to S3.
   * After this method called, upload a file via presigned URL.
   */
  def addViaPresignedUrl(file: File#WithNoId): Future[(File.Id, String)] =
    for {
      client    <- getClient
      url       <- client.genPresignedUrlForUpload(file.v)
      Some(fid) <- master.run(
        fileTable returning fileTable.map(_.id) += file.v
      )
    } yield (File.Id(fid), url.toString())

  /**
   * Update the file information.
   * At the same time upload a specified file to S3.
   */
  def update(file: EntityEmbeddedId, content: java.io.File): Future[Option[EntityEmbeddedId]] =
    for {
      client <- getClient
      _      <- client.upload(file.v, content)
      old    <- master.run {
        for {
          old <- fileTable.filter(_.id === file.id).result.headOption
          _   <- fileTable.filter(_.id === file.id).update(file.v)
        } yield old
      }
    } yield old

  /**
   * Update the file information.
   * However, the file body content has not yet been uploaded to S3.
   * After this method called, upload a file via presigned URL.
   */
  def updateViaPresignedUrl(file: EntityEmbeddedId): Future[(Option[EntityEmbeddedId], String)] =
    for {
      client <- getClient
      url    <- client.genPresignedUrlForUpload(file.v)
      old    <- master.run {
        for {
          old <- fileTable.filter(_.id === file.id).result.headOption
          _   <- fileTable.filter(_.id === file.id).update(file.v)
        } yield old
      }
    } yield (old, url.toString)

  /**
   * Update the sepecified file's image size.
   */
  def updateImageSize(fid: File.Id, size: File.ImageSize): Future[Option[EntityEmbeddedId]] =
    master.run {
      for {
        old <- fileTable.filter(_.id === fid).result.headOption
        _   <- fileTable.filter(_.id === fid).map(
          c => (c.width, c.height)
        ).update((Some(size.width), Some(size.height)))
      } yield old
    }

  // --[ Methods ]--------------------------------------------------------------
  /**
   * Remove the file information.
   */
  def remove(id: File.Id): Future[Option[EntityEmbeddedId]] =
    master.run {
      for {
        old <- fileTable.filter(_.id === id).result.headOption
        _   <- fileTable.filter(_.id === id).delete
      } yield old
    }

  /**
   * Remove the file information list.
   */
  def bulkRemove(idSeq: Seq[File.Id]): Future[Seq[EntityEmbeddedId]] =
    master.run {
      val rows = fileTable.filter(_.id inSet idSeq)
      for {
        oldSeq <- rows.result
        _      <- rows.delete
      } yield oldSeq
    }

  /**
   * Erase the file information and a physical file object at S3.
   */
  def erase(id: File.Id): Future[Option[EntityEmbeddedId]] =
    for {
      fileOpt <- master.run {
        for {
          old <- fileTable.filter(_.id === id).result.headOption
          _   <- fileTable.filter(_.id === id).delete
        } yield old
      }
      _ <- fileOpt match {
        case None       => Future.successful(())
        case Some(file) => getClient.map(_.remove(file))
      }
    } yield fileOpt

  /**
   * Erase the file information list and the physical file object list at S3.
   */
  def bulkErase(idSeq: Seq[File.Id]): Future[Seq[EntityEmbeddedId]] =
    for {
      fileSeq <- master.run {
        val rows = fileTable.filter(_.id inSet idSeq)
        for {
          oldSeq <- rows.result
          _      <- rows.delete
        } yield oldSeq
      }
      _ <- getClient.map(_.bulkRemove(fileSeq.head.bucket, fileSeq))
    } yield fileSeq
}
