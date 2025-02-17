/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.util.json

import scala.util.{ Try, Success, Failure }

import play.api.libs.json._
import play.api.libs.json.EnvReads

import ixias.model.EntityId
import ixias.util.*

/** Reads Conbinator for type conversion in service
  */
trait JsonEnvReads extends EnvReads {

  given [L <: EntityId.IdLong]: Reads[L] = {
    case JsNumber(n) if n.isValidLong => {
      val Id = EntityId.IdLong(n.toLong).asInstanceOf[L]
      JsSuccess(Id)
    }
    case JsNumber(n) => JsError("error.expected.tag.long")
    case _           => JsError("error.expected.tag.jsnumber")
  }

  given [S <: EntityId.IdString]: Reads[S] = {
    case JsString(s) => {
      val Id = EntityId.IdString(s).asInstanceOf[S]
      JsSuccess(Id)
    }
    case _ => JsError("error.expected.tag.jsstring")
  }

  /** Deserializer for ixias.util.EnumStatus
    */
  def enumReads[E <: EnumStatus](gen: EnumStatusGen[E]): Reads[E] =
  {
    case JsNumber(n) if n.isValidShort => JsSuccess(gen.findByCode(n.toShort))
    case JsNumber(n) => JsError("error.expected.enum.short")
    case _ => JsError("error.expected.enum.jsnumber")
  }

  /** Deserializer for ixias.util.EnumBitFlags
    */
  def enumReads[E <: EnumBitFlags](gen: EnumBitFlagsGen[E]): Reads[Seq[E]] =
  {
    case JsNumber(n) if n.isValidLong => JsSuccess(gen(n.toLong))
    case JsNumber(n) => JsError("error.expected.enum.long")
    case _ => JsError("error.expected.enum.jsnumber")
  }

  /** Deserializer for ixias.util.EnumBitFlags
    */
  def enumBitFlagsReads[E <: EnumBitFlags](gen: EnumBitFlagsGen[E]): Reads[Seq[E]] =
    (json: JsValue) =>
      json.validate[Seq[Long]] match {
        case JsSuccess(nlist, _) => JsSuccess(gen(nlist.sum))
        case JsError(e)          => JsError(e)
      }

  /** Deserializer for java.time.YearMonth
    */
  implicit object YearMonthReads extends Reads[java.time.YearMonth] {
    def reads(json: JsValue) = json match {
      case JsNumber(millis) =>
        JsSuccess(
          java.time.YearMonth.now(
            java.time.Clock.fixed(
              java.time.Instant.ofEpochMilli(millis.toLong),
              java.time.ZoneOffset.UTC
            )
          )
        )
      case JsString(s) => {
        val fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")
        Try(java.time.YearMonth.parse(s, fmt)) match {
          case Success(v) => JsSuccess(v)
          case Failure(_) => JsError(JsonValidationError("error.expected.date.format", fmt))
        }
      }
      case _ =>
        for {
          v1 <- (json \ "year").validate[Int]
          v2 <- (json \ "month").validate[Int]
        } yield java.time.YearMonth.of(v1, v2)
    }
  }
}
