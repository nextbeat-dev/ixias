/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.s3

import ixias.aws.s3.model.File
import ixias.aws.s3.persistence.SlickResource
import ixias.slick.SlickRepository
import ixias.slick.jdbc.MySQLProfile.api._
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.services.s3.model.GetObjectResponse

import scala.concurrent.Future

// S3 management repository
//~~~~~~~~~~~~~~~~~~~~~~~~~~~
trait AmazonS3Repository extends SlickRepository[File] with SlickResource {

  def master: Database
  def slave:  Database

  protected lazy val client: AmazonS3Client = AmazonS3Client(dsn)

  // --[ Methods ]--------------------------------------------------------------
  /** Get file object.
    */
  def get(id: File.Id): Future[Option[EntityEmbeddedId]] =
    slave.run[Option[File]](fileTable.filter(_.id === id).result.headOption)

  private def genPreSignedUrlForAccess(file: File): Future[java.net.URL] =
    Future.fromTry(client.generateGetPreSignedUrl(file.bucket, file.key, getPresignedUrlTimeoutForGet))

  private def genPreSignedUrlForUpload(file: File): Future[java.net.URL] =
    Future.fromTry(client.generateUploadPreSignedUrl(file.bucket, file.key, getPresignedUrlTimeoutForPut))

  /** Get file object with a pre-signed URL for accessing an Amazon S3 resource.
    */
  def getWithPresigned(id: File.Id): Future[Option[EntityEmbeddedId]] =
    get(id) flatMap {
      case None => Future.successful(None)
      case Some(file) =>
        for {
          url <- genPreSignedUrlForAccess(file.v)
        } yield Some(file.map(_.copy(presignedUrl = Some(url))))
    }

  /** Get file object as `Entity` with a input stream for it.
    */
  def getWithContent(id: File.Id): Future[Option[(EntityEmbeddedId, ResponseInputStream[GetObjectResponse])]] =
    get(id) flatMap {
      case None => Future.successful(None)
      case Some(file) =>
        for {
          s3object <- Future.fromTry(client.load(file.v.bucket, file.v.key))
        } yield Some((file, s3object))
    }

  /** Finds file objects by set of file ids.
    */
  def filter(ids: Seq[File.Id]): Future[Seq[EntityEmbeddedId]] =
    slave.run[Seq[File]](fileTable.filter(_.id inSet ids).result)

  /** Finds file objects with a pre-signed URL by set of file ids.
    */
  def filterWithPresigned(ids: Seq[File.Id]): Future[Seq[EntityEmbeddedId]] =
    for {
      fileSeq1 <- filter(ids)
      fileSeq2 <- Future.sequence(fileSeq1.map { file =>
                    for {
                      url <- genPreSignedUrlForAccess(file.v)
                    } yield file.v.copy(presignedUrl = Some(url))
                  })
    } yield fileSeq2

  /** Save the file information. At the same time upload a specified file to S3.
    */
  def add(file: File.WithNoId, content: java.io.File): Future[File.Id] =
    for {
      _ <- Future.fromTry(client.upload(file.v.bucket, file.v.key, content))
      case Some(fid) <- master.run(
                     fileTable returning fileTable.map(_.id) += file.v
                   )
    } yield fid

  /** Save the file information. However, the file body content has not yet been uploaded to S3. After this method
    * called, upload a file via presigned URL.
    */
  def addViaPresignedUrl(file: File.WithNoId): Future[(File.Id, String)] =
    for {
      url <- genPreSignedUrlForUpload(file.v)
      case Some(fid) <- master.run(
                     fileTable returning fileTable.map(_.id) += file.v
                   )
    } yield (fid, url.toString)

  /** Update the file information. At the same time upload a specified file to S3.
    */
  def update(file: EntityEmbeddedId, content: java.io.File): Future[Option[EntityEmbeddedId]] =
    for {
      _ <- Future.fromTry(client.upload(file.v.bucket, file.v.key, content))
      old <- master.run {
               for {
                 old <- fileTable.filter(_.id === file.id).result.headOption
                 _   <- fileTable.filter(_.id === file.id).update(file.v)
               } yield old
             }
    } yield old

  /** Update the file information. However, the file body content has not yet been uploaded to S3. After this method
    * called, upload a file via presigned URL.
    */
  def updateViaPresignedUrl(file: EntityEmbeddedId): Future[(Option[EntityEmbeddedId], String)] =
    for {
      url <- genPreSignedUrlForUpload(file.v)
      old <- master.run {
               for {
                 old <- fileTable.filter(_.id === file.id).result.headOption
                 _   <- fileTable.filter(_.id === file.id).update(file.v)
               } yield old
             }
    } yield (old, url.toString)

  /** Update the sepecified file's image size.
    */
  def updateImageSize(fid: File.Id, size: File.ImageSize): Future[Option[EntityEmbeddedId]] =
    master.run {
      for {
        old <- fileTable.filter(_.id === fid).result.headOption
        _ <- fileTable.filter(_.id === fid).map(c => (c.width, c.height)).update((Some(size.width), Some(size.height)))
      } yield old
    }
  // --[ Methods ]--------------------------------------------------------------
  /** Remove the file information.
    */
  def remove(id: File.Id): Future[Option[EntityEmbeddedId]] =
    master.run {
      for {
        old <- fileTable.filter(_.id === id).result.headOption
        _   <- fileTable.filter(_.id === id).delete
      } yield old
    }

  /** Remove the file information list.
    */
  def bulkRemove(idSeq: Seq[File.Id]): Future[Seq[EntityEmbeddedId]] =
    master.run {
      val rows = fileTable.filter(_.id inSet idSeq)
      for {
        oldSeq <- rows.result
        _      <- rows.delete
      } yield oldSeq
    }

  /** Erase the file information and a physical file object at S3.
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
             case Some(file) => Future.fromTry(client.remove(file.bucket, file.key))
           }
    } yield fileOpt

  /** Erase the file information list and the physical file object list at S3.
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
      _ <- Future.fromTry(client.bulkRemove(fileSeq.head.bucket, fileSeq.map(_.key): _*))
    } yield fileSeq
}
