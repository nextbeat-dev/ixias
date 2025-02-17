/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.slick.jdbc

import java.time.format.DateTimeFormatter
import java.time.{ LocalTime, LocalDate, LocalDateTime, YearMonth, Duration }
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

import java.sql.{ PreparedStatement, ResultSet }

import slick.ast.BaseTypedType
import slick.jdbc.{ MySQLProfile => SlickMySQLProfile, JdbcType }

import ixias.slick.lifted._

trait MySQLProfile extends SlickMySQLProfile {
  self =>

  override val columnTypes: self.MySQLJdbcTypes = new CustomMySQLJdbcTypes

  /** Copied from
    * https://github.com/slick/slick/blob/v3.3.2/slick/src/main/scala/slick/jdbc/MySQLProfile.scala#L323-L345
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
          case '"'  => sb append "\\\""
          case 0    => sb append "\\0"
          case 26   => sb append "\\Z"
          case '\b' => sb append "\\b"
          case '\n' => sb append "\\n"
          case '\r' => sb append "\\r"
          case '\t' => sb append "\\t"
          case '\\' => sb append "\\\\"
          case _    => sb append c
        }
        sb append '\''
        sb.toString
    }

  private class CustomMySQLJdbcTypes extends self.MySQLJdbcTypes {
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

  trait SlickColumnTypeOps {

    self: JdbcAPI =>

    // --[ Ixias Enum ]-----------------------------------------------------------
    // Short <-> ixias.util.EnumStatus
    implicit def ixiasEnumStatusColumnType[T <: ixias.util.EnumStatus](implicit
      ctag: reflect.ClassTag[T]
    ): JdbcType[T] with BaseTypedType[T] =
      MappedColumnType.base[T, Short](
        enum => enum.code,
        code => {
          val clazz =
            Class.forName(ctag.runtimeClass.getName + "$", true, Thread.currentThread().getContextClassLoader())
          val module = clazz.getField("MODULE$").get(null)
          val method = clazz.getMethod("apply", classOf[Short])
          val `enum` = method.invoke(module, code.asInstanceOf[AnyRef])
          `enum`.asInstanceOf[T]
        }
      )

    // Long <-> Seq[ixias.util.EnumBitFlags]
    implicit def ixiasEnumBitsetSeqColumnType[T <: ixias.util.EnumBitFlags](implicit
      ctag: reflect.ClassTag[T]
    ): JdbcType[Seq[T]] with BaseTypedType[Seq[T]] = {
      val clazz  = Class.forName(ctag.runtimeClass.getName + "$", true, Thread.currentThread().getContextClassLoader())
      val module = clazz.getField("MODULE$").get(null)
      MappedColumnType.base[Seq[T], Long](
        bitset => {
          val method = clazz.getMethod("toBitset", classOf[Seq[_]])
          val code   = method.invoke(module, bitset.asInstanceOf[AnyRef])
          code.asInstanceOf[Long]
        },
        code => {
          val method = clazz.getMethod("apply", classOf[Long])
          val bitset = method.invoke(module, code.asInstanceOf[AnyRef])
          bitset.asInstanceOf[Seq[T]]
        }
      )
    }

    // --[ Ixias Id ]-------------------------------------------------------------
    // Long <-> ixias.model.@@[Long, _]
    implicit def ixiasIdAsLongColumnType[T <: ixias.model.@@[Long, _]](implicit
      ctag: reflect.ClassTag[T]
    ): JdbcType[T] with BaseTypedType[T] = {
      val Id = ixias.model.the[ixias.model.Identity[T]]
      MappedColumnType.base[T, Long](
        id => id.asInstanceOf[Long],
        value => Id(value.asInstanceOf[T])
      )
    }

    // String <-> ixias.model.@@[String, _]
    implicit def ixiasIdAsStringColumnType[T <: ixias.model.@@[String, _]](implicit
      ctag: reflect.ClassTag[T]
    ): JdbcType[T] with BaseTypedType[T] = {
      val Id = ixias.model.the[ixias.model.Identity[T]]
      MappedColumnType.base[T, String](
        id => id.asInstanceOf[String],
        value => Id(value.asInstanceOf[T])
      )
    }

    // --[ Java8 Time ]-----------------------------------------------------------
    // java.sql.Timestamp <-> java.time.LocalDateTime
    implicit val javaLocalDateTimeColumnType: JdbcType[LocalDateTime] with BaseTypedType[LocalDateTime] =
      MappedColumnType.base[java.time.LocalDateTime, java.sql.Timestamp](
        dt => java.sql.Timestamp.valueOf(dt),
        ts => ts.toLocalDateTime()
      )

    // java.sql.Date <-> java.time.LocalDate
    implicit val javaLocalDateColumnType: JdbcType[LocalDate] with BaseTypedType[LocalDate] =
      MappedColumnType.base[java.time.LocalDate, java.sql.Date](
        ld => java.sql.Date.valueOf(ld),
        d => d.toLocalDate()
      )

    // java.sql.Date <-> java.time.YearMonth
    implicit val javaYearMonthColumnType: JdbcType[YearMonth] with BaseTypedType[YearMonth] =
      MappedColumnType.base[java.time.YearMonth, java.sql.Date](
        ld => java.sql.Date.valueOf(ld.atDay(1)),
        d => java.time.YearMonth.from(d.toLocalDate())
      )

    // java.sql.Time <-> java.time.LocalTime
    implicit val javaLocalTimeColumnType: JdbcType[LocalTime] with BaseTypedType[LocalTime] =
      MappedColumnType.base[java.time.LocalTime, java.sql.Time](
        lt => java.sql.Time.valueOf(lt),
        t => t.toLocalTime()
      )

    // java.sql.Time <-> java.time.Duration
    implicit val javaDurationColumnType: MappedJdbcType[Duration, String] with BaseTypedType[Duration] =
      new MappedJdbcType[java.time.Duration, String] with slick.ast.BaseTypedType[java.time.Duration] {
        import java.util.TimeZone
        import java.time.Duration
        override def sqlType:                        Int    = java.sql.Types.VARCHAR
        override def valueToSQLLiteral(d: Duration): String = "{ t '" + map(d) + "' }"
        override def getValue(r: java.sql.ResultSet, idx: Int): Duration = {
          val v = r.getTimestamp(idx)
          (v.asInstanceOf[AnyRef] eq null) || tmd.wasNull(r, idx) match {
            case true  => null.asInstanceOf[Duration]
            case false => Duration.ofMillis(v.getTime + TimeZone.getDefault.getRawOffset)
          }
        }
        def comap(str: String): Duration = {
          val secs = java.time.LocalDateTime.parse(str).getSecond.toLong
          Duration.ofSeconds(secs + TimeZone.getDefault.getRawOffset)
        }
        def map(d: Duration): String = "%02d:%02d:%02d".format(
          d.toHours,
          d.toMinutes  % 60,
          d.getSeconds % 60
        )
      }
  }

  override val api
    : JdbcAPI with Aliases with ConverterOps with SlickColumnTypeOps with SlickColumnType with SlickQueryOps =
    new JdbcAPI with Aliases with ConverterOps with SlickColumnTypeOps with SlickColumnType with SlickQueryOps {}
}

object MySQLProfile extends MySQLProfile
