/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

import cats.data.{ NonEmptyList, Validated, ValidatedNel }
import ixias.util.Logging
import play.api.libs.typedmap.TypedKey
import play.api.mvc.{ RequestHeader, Result }

import scala.language.implicitConversions
import scala.quoted.{ Quotes, Type }

// Helper to get header attrs
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
object RequestHeaderAttrHelper extends Logging {
  import Errors._

  /** Retrieves and validate a value of the specified key.
    */
  def getValue[T](key: TypedKey[T])(implicit rh: RequestHeader, tpe: Type[T], q: Quotes): ValidatedNel[String, T] =
    rh.attrs.get(key) match {
      case Some(v) => Validated.Valid(v)
      case None => {
        Validated.Invalid(
          NonEmptyList.of(
            "The value under the specified key was not found. Entity type is "
              + Type.show[T]
          )
        )
      }
    }

  /** Implicit convert. ValidatedNel to Either
    */
  implicit def toEither[T](validated: ValidatedNel[String, T]): Either[Result, T] =
    validated match {
      case Validated.Valid(v) => Right(v)
      case Validated.Invalid(nel) => {
        nel.map(invalid => logger.warn(invalid))
        Left(E_NOT_FOUND)
      }
    }

  /** case Tuple1 */
  def get[T1](a1: TypedKey[T1])(implicit rh: RequestHeader, tpe1: Type[T1], q: Quotes): Either[Result, T1] =
    getValue(a1)

  /** case Tuple2 */
  import cats.implicits._
  def get[T1, T2](a1: TypedKey[T1], a2: TypedKey[T2])(implicit
    rh:   RequestHeader,
    tpe1: Type[T1],
    tpe2: Type[T2],
    q:    Quotes
  ): Either[Result, (T1, T2)] =
    (getValue(a1), getValue(a2))
      .mapN((_, _))

  /** case Tuple3 */
  def get[T1, T2, T3](a1: TypedKey[T1], a2: TypedKey[T2], a3: TypedKey[T3])(implicit
    rh:   RequestHeader,
    tpe1: Type[T1],
    tpe2: Type[T2],
    tpe3: Type[T3],
    q:    Quotes
  ): Either[Result, (T1, T2, T3)] =
    (getValue(a1), getValue(a2), getValue(a3))
      .mapN((_, _, _))

  /** case Tuple4 */
  def get[T1, T2, T3, T4](a1: TypedKey[T1], a2: TypedKey[T2], a3: TypedKey[T3], a4: TypedKey[T4])(implicit
    rh:   RequestHeader,
    tpe1: Type[T1],
    tpe2: Type[T2],
    tpe3: Type[T3],
    tpe4: Type[T4],
    q:    Quotes
  ): Either[Result, (T1, T2, T3, T4)] =
    (getValue(a1), getValue(a2), getValue(a3), getValue(a4))
      .mapN((_, _, _, _))

  /** case Tuple5 */
  def get[T1, T2, T3, T4, T5](a1: TypedKey[T1], a2: TypedKey[T2], a3: TypedKey[T3], a4: TypedKey[T4], a5: TypedKey[T5])(
    implicit
    rh:   RequestHeader,
    tpe1: Type[T1],
    tpe2: Type[T2],
    tpe3: Type[T3],
    tpe4: Type[T4],
    tpe5: Type[T5],
    q:    Quotes
  ): Either[Result, (T1, T2, T3, T4, T5)] =
    (getValue(a1), getValue(a2), getValue(a3), getValue(a4), getValue(a5))
      .mapN((_, _, _, _, _))
}
