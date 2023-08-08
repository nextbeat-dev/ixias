/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.lifted

import org.joda.time.{DateTime, Duration, LocalDate, LocalTime}
import slick.ast.BaseTypedType
import slick.jdbc.{JdbcProfile, JdbcType}

import java.time
import java.time.{LocalDateTime, YearMonth}

trait SlickColumnTypeOps[P <: JdbcProfile] {
  val driver: P
  import driver.api._

  // --[ Ixias Enum ]-----------------------------------------------------------
  // Short <-> ixias.util.EnumStatus
  implicit def ixiasEnumStatusColumnType[T <: ixias.util.EnumStatus](implicit ctag: reflect.ClassTag[T]): JdbcType[T] with BaseTypedType[T] =
    MappedColumnType.base[T, Short](
      enum => enum.code,
      code => {
        val clazz  = Class.forName(ctag.runtimeClass.getName + "$", true, Thread.currentThread().getContextClassLoader())
        val module = clazz.getField("MODULE$").get(null)
        val method = clazz.getMethod("apply", classOf[Short])
        val `enum` = method.invoke(module, code.asInstanceOf[AnyRef])
        `enum`.asInstanceOf[T]
      }
    )

  // Long <-> Seq[ixias.util.EnumBitFlags]
  implicit def ixiasEnumBitsetSeqColumnType[T <: ixias.util.EnumBitFlags](implicit ctag: reflect.ClassTag[T]): JdbcType[Seq[T]] with BaseTypedType[Seq[T]] = {
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
  implicit def ixiasIdAsLongColumnType[T <: ixias.model.@@[Long, _]](implicit ctag: reflect.ClassTag[T]): JdbcType[T] with BaseTypedType[T] = {
    val Id = ixias.model.the[ixias.model.Identity[T]]
    MappedColumnType.base[T, Long](
      id    => id.asInstanceOf[Long],
      value => Id(value.asInstanceOf[T])
    )
  }

  // String <-> ixias.model.@@[String, _]
  implicit def ixiasIdAsStringColumnType[T <: ixias.model.@@[String, _]](implicit ctag: reflect.ClassTag[T]): JdbcType[T] with BaseTypedType[T] = {
    val Id = ixias.model.the[ixias.model.Identity[T]]
    MappedColumnType.base[T, String](
      id    => id.asInstanceOf[String],
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
  implicit val javaLocalDateColumnType: JdbcType[time.LocalDate] with BaseTypedType[time.LocalDate] =
    MappedColumnType.base[java.time.LocalDate, java.sql.Date](
      ld => java.sql.Date.valueOf(ld),
      d  => d.toLocalDate()
    )

  // java.sql.Date <-> java.time.YearMonth
  implicit val javaYearMonthColumnType: JdbcType[YearMonth] with BaseTypedType[YearMonth] =
    MappedColumnType.base[java.time.YearMonth, java.sql.Date](
      ld => java.sql.Date.valueOf(ld.atDay(1)),
      d  => java.time.YearMonth.from(d.toLocalDate())
    )

  // java.sql.Time <-> java.time.LocalTime
  implicit val javaLocalTimeColumnType: JdbcType[time.LocalTime] with BaseTypedType[time.LocalTime] =
    MappedColumnType.base[java.time.LocalTime, java.sql.Time](
      lt => java.sql.Time.valueOf(lt),
      t  => t.toLocalTime()
    )

  // java.sql.Time <-> java.time.Duration
  implicit val javaDurationColumnType: driver.MappedJdbcType[time.Duration, String] with BaseTypedType[time.Duration] =
    new driver.MappedJdbcType[java.time.Duration, String] with slick.ast.BaseTypedType[java.time.Duration] {
      import java.util.TimeZone
      import java.time.Duration
      override def sqlType: Int = java.sql.Types.VARCHAR
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
        d.toMinutes % 60,
        d.getSeconds % 60
      )
    }

  // --[ Joda Time ]------------------------------------------------------------
  // java.sql.Timestamp <-> org.joda.time.DateTime
  implicit val jodaDateTimeColumnType: JdbcType[DateTime] with BaseTypedType[DateTime] =
    MappedColumnType.base[org.joda.time.DateTime, java.sql.Timestamp](
      dt => new java.sql.Timestamp(dt.getMillis),
      ts => new org.joda.time.DateTime(ts.getTime)
    )

  // java.sql.Date <-> org.joda.time.LocalDate
  implicit val jodaLocalDateColumnType: JdbcType[LocalDate] with BaseTypedType[LocalDate] =
    MappedColumnType.base[org.joda.time.LocalDate, java.sql.Date](
      ld => new java.sql.Date(ld.toDateTimeAtStartOfDay(org.joda.time.DateTimeZone.UTC).getMillis),
      d  => new org.joda.time.LocalDate(d.getTime)
    )

  // java.sql.Time <-> org.joda.time.LocalTime
  implicit val jodaLocalTimeColumnType: JdbcType[LocalTime] with BaseTypedType[LocalTime] =
    MappedColumnType.base[org.joda.time.LocalTime, java.sql.Time](
      lt => new java.sql.Time(lt.toDateTimeToday.getMillis),
      t  => new org.joda.time.LocalTime(t, org.joda.time.DateTimeZone.UTC)
    )

  // java.sql.Time <-> org.joda.time.Duration
  implicit val jodaDurationColumnType: driver.MappedJdbcType[Duration, String] with BaseTypedType[Duration] =
    new driver.MappedJdbcType[org.joda.time.Duration, String] with slick.ast.BaseTypedType[org.joda.time.Duration] {
      import java.util.TimeZone
      import org.joda.time.Duration
      override def sqlType = java.sql.Types.VARCHAR
      override def valueToSQLLiteral(d: Duration) = "{ ts '" + map(d) + "' }"
      override def getValue(r: java.sql.ResultSet, idx: Int) = {
        val v = r.getTimestamp(idx)
          (v.asInstanceOf[AnyRef] eq null) || tmd.wasNull(r, idx) match {
            case true  => null.asInstanceOf[Duration]
            case false => new Duration(v.getTime + TimeZone.getDefault.getRawOffset)
          }
      }
      def comap(str: String) = {
        val millis = org.joda.time.DateTime.parse(str).getMillisOfSecond.toLong
        new Duration(millis + TimeZone.getDefault.getRawOffset)
      }
      def map(d: Duration) = "%02d:%02d:%02d".format(
        d.getStandardHours,
        d.getStandardMinutes % 60,
        d.getStandardSeconds % 60
      )
    }
}
