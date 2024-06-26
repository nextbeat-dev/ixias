/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc.binder

import play.api.mvc.QueryStringBindable
import ixias.play.api.mvc.QueryStringHelper

trait CursorBindable {

  // --[ Alias ]----------------------------------------------------------------
  val Cursor = ixias.model.Cursor
  type Cursor = ixias.model.Cursor

  // -- [ QueryStringBindable ] ------------------------------------------------
  /** QueryString binder for `Cursor`
    */
  case class queryStringBindableCursor(
    limitDefault: Int, // The number of results: default value
    limitMax:     Int  // The number of results: maximum value
  ) extends QueryStringBindable[Cursor]
       with QueryStringHelper {

    /** Unbind a query string parameter.
      */
    def unbind(key: String, value: Cursor): String =
      Seq(
        key + ".offset" -> Option(value.offset),
        key + ".limit"  -> value.limit
      ).collect({
        case (_key, Some(v)) if v > 0 => "%s=%d".format(_key, v)
      }).mkString("&")

    /** Bind a query string parameter.
      */
    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Cursor]] = {
      implicit val _params = params
      (for {
        v1 <- implicitly[QueryStringBindable[Long]]._bindOption(key + ".offset")
        v2 <- implicitly[QueryStringBindable[Long]]._bindOption(key + ".limit")
      } yield v1.filter(_ > 0) -> v2.filter(_ > 0) match {
        case (Some(v1), Some(v2)) => Cursor(v1, Some(v2))
        case (None, Some(v2))     => Cursor(0L, Some(v2))
        case (Some(v1), None)     => Cursor(v1, Some(limitDefault.toLong))
        case _                    => Cursor(0L, Some(limitDefault.toLong))
      }) match {
        case Left(v) => Some(Left(v))
        case Right(cur) =>
          cur.limit.map(_ > limitMax) match {
            case Some(true) => Some(Right(cur.copy(limit = Some(limitMax.toLong))))
            case _          => Some(Right(cur))
          }
      }
    }
  }
}
