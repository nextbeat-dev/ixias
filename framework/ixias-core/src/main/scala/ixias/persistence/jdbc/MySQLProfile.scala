/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.jdbc

import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

import java.sql.{ PreparedStatement, ResultSet }

import slick.jdbc.{ MySQLProfile => SlickMySQLProfile }

trait MySQLProfile extends SlickMySQLProfile {
  self =>

  override val columnTypes: self.JdbcTypes = new CustomMySQLJdbcTypes

  /**
   * Copied from https://github.com/slick/slick/blob/v3.3.2/slick/src/main/scala/slick/jdbc/MySQLProfile.scala#L323-L345
   */
  @inline
  private def stringToMySqlString(value: String): String =
    value match {
      case null => "NULL"
      case _ =>
        val sb = new StringBuilder
        sb append '\''
        for (c <- value) c match {
          case '\'' => sb append "\\'"
          case '"' => sb append "\\\""
          case 0 => sb append "\\0"
          case 26 => sb append "\\Z"
          case '\b' => sb append "\\b"
          case '\n' => sb append "\\n"
          case '\r' => sb append "\\r"
          case '\t' => sb append "\\t"
          case '\\' => sb append "\\\\"
          case _ => sb append c
        }
        sb append '\''
        sb.toString
    }

  private class CustomMySQLJdbcTypes extends self.JdbcTypes {
    private val formatter =
      new DateTimeFormatterBuilder()
        .append(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        .optionalStart()
        .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
        .optionalEnd()
        .toFormatter()

    override val localDateTimeType: LocalDateTimeJdbcType = new LocalDateTimeJdbcType {
      override def sqlType: Int = java.sql.Types.VARCHAR

      override def setValue(v: LocalDateTime, p: PreparedStatement, idx: Int): Unit =
        p.setString(idx, if (v == null) null else v.toString)

      override def getValue(r: ResultSet, idx: Int): LocalDateTime =
        r.getString(idx) match {
          case null => null
          // Pass formatter to parse from string to date type
          case dateString => LocalDateTime.parse(dateString, formatter)
        }

      override def updateValue(v: LocalDateTime, r: ResultSet, idx: Int) =
        r.updateString(idx, if (v == null) null else v.toString)

      override def valueToSQLLiteral(value: LocalDateTime): String =
        stringToMySqlString(value.toString)
    }
  }
}

object MySQLProfile extends MySQLProfile
