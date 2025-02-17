/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.s3.persistence

import java.time.LocalDateTime

import ixias.slick.jdbc.MySQLProfile.api.{ *, given }

import ixias.aws.s3.model.File
import ixias.aws.s3.AmazonS3Config

trait SlickResource extends AmazonS3Config {

  val DataSourceName = ixias.aws.DataSourceName
  type DataSourceName = ixias.aws.DataSourceName
  implicit val dsn: DataSourceName

  protected class FileTable(tag: Tag) extends Table[File](tag, getMetaTableName) {

    // Columns
    /* @1 */
    def id = column[Option[File.Id]]("id", UInt64, O.PrimaryKey, O.AutoInc)
    /* @2 */
    def region = column[String]("region", Utf8Char32)
    /* @3 */
    def bucket = column[String]("bucket", Utf8Char32)
    /* @4 */
    def key = column[String]("key", Utf8Char255)
    /* @5 */
    def typedef = column[String]("typedef", Utf8Char32)
    /* @6 */
    def width = column[Option[Int]]("width", UInt16)
    /* @7 */
    def height = column[Option[Int]]("height", UInt16)
    /* @8 */
    def updatedAt = column[LocalDateTime]("updated_at", TsCurrent)
    /* @9 */
    def createdAt = column[LocalDateTime]("created_at", Ts)

    // Indexes
    def ukey01 = index("key01", (bucket, key, region), unique = true)

    // All columns as a tuple
    import File._
    type TableElementTuple = (
      Option[File.Id],
      String,
      String,
      String,
      String,
      Option[Int],
      Option[Int],
      LocalDateTime,
      LocalDateTime
    )

    // The * projection of the table
    def * = (id, region, bucket, key, typedef, width, height, updatedAt, createdAt).<>(
      /* The bidirectional mappings : Tuple(table) => Model */
      (t: TableElementTuple) => {
        val imageSize = (t._6, t._7) match {
          case (Some(width), Some(height)) => Some(ImageSize(width, height))
          case _                           => None
        }
        File(t._1, t._2, t._3, t._4, t._5, imageSize, None, t._8, t._9)
      },
      /* The bidirectional mappings : Model => Tuple(table) */
      (v: TableElementType) =>
        val t = File.unapply(v)
        (
          t._1,
          t._2,
          t._3,
          t._4,
          t._5,
          t._6.map(_.width),
          t._6.map(_.height),
          LocalDateTime.now(),
          t._9
        )
    )
  }

  // --[ Tables ] --------------------------------------------------------------
  protected val fileTable = TableQuery[FileTable]
}
