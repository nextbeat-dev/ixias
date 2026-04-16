/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.model

import ixias.persistence.lifted._
import slick.jdbc.JdbcProfile
import java.time.LocalDateTime

trait Table[R, P <: JdbcProfile] { self =>

  //-- [ Required properties ] -------------------------------------------------
  /** The configured driver. */
  val driver: P

  /** The map of DSN as string. */
  val dsn: Map[String, DataSourceName]

  /** The table query. */
  val query: Query

  /** The alias for DSN */
  val DataSourceName = ixias.persistence.model.DataSourceName

  //-- [ Table Manifest ] ------------------------------------------------------
  /** The type of table row. */
  type Record = R

  /** A Tag marks a specific row represented by an AbstractTable instance. */
  type Tag = slick.lifted.Tag

  /** The type of all table row objects. */
  type Table      <: driver.Table[Record]
  type BasicTable =  driver.Table[Record]

  //-- [ Table Query ] ---------------------------------------------------------
  /**
   * Represents a database table. Implementation class add extension methods to TableQuery
   * for operations that can be performed on tables but not on arbitrary queries.
   */
  type Query      <: slick.lifted.TableQuery[Table]
  type BasicQuery =  slick.lifted.TableQuery[Table]

  //-- [ Utility Methods ] -----------------------------------------------------
  /**
   * The API for using the utility methods with a single import statement.
   * This provides the repository's implicits, the Database connections,
   * and commonly types and objects.
   */
  trait API extends driver.API
      with Aliases
      with ExtensionMethods
      with SlickColumnOptionOps
      with SlickColumnTypeOps[P]
      with SlickRepOps[P] {
    lazy val driver = self.driver

    // Slick's MySQL LocalDateTime mapping uses VARCHAR/ISO parsing, but this codebase
    // persists DATETIME columns and expects java.sql.Timestamp semantics.
    override implicit val localDateTimeColumnType: driver.columnTypes.LocalDateTimeJdbcType =
      new driver.columnTypes.LocalDateTimeJdbcType {
        override def sqlType: Int = java.sql.Types.TIMESTAMP
        override def setValue(v: LocalDateTime, p: java.sql.PreparedStatement, idx: Int): Unit =
          p.setTimestamp(idx, if (v == null) null else java.sql.Timestamp.valueOf(v))
        override def getValue(r: java.sql.ResultSet, idx: Int): LocalDateTime =
          r.getTimestamp(idx) match {
            case null      => null
            case timestamp => timestamp.toLocalDateTime
          }
        override def updateValue(v: LocalDateTime, r: java.sql.ResultSet, idx: Int): Unit =
          r.updateTimestamp(idx, if (v == null) null else java.sql.Timestamp.valueOf(v))
        override def valueToSQLLiteral(value: LocalDateTime): String =
          s"'${java.sql.Timestamp.valueOf(value)}'"
      }
  }
  trait APIUnsafe extends API with SlickRepUnsafeOps[P]
  val api:       API       = new API       {}
  val apiUnsafe: APIUnsafe = new APIUnsafe {}
}
