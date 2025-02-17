/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc.binder

import play.api.mvc.{PathBindable, QueryStringBindable}

import ixias.model.EntityId

/** Binder utility function for ID implemented in Tagged-Type
  *
  * <Usage > type UserId = Long @@ UserModel implicit val pathBindForUserId = pathBindableBoxId[UserId] implicit val
  * queryBindForUserId = queryStringBindableBoxId[UserId]
  */
trait IdBindable extends Box {

  // -- [ PathBindable ] -------------------------------------------------------
  /** PathBindable: ixias.model.EntityId.Id
    */
  def pathBindableBoxId[T <: EntityId.Id](implicit ctag: reflect.ClassTag[T]): PathBindable[Box[T]] = {
    new PathBindable.Parsing[Box[T]](
      (s: String) => {
        val id = ctag.runtimeClass match {
          case x if classOf[Int].isAssignableFrom(x)    => EntityId.IdLong(s.toInt).asInstanceOf[T]
          case x if classOf[Long].isAssignableFrom(x)   => EntityId.IdLong(s.toLong).asInstanceOf[T]
          case x if classOf[String].isAssignableFrom(x) => EntityId.IdString(s).asInstanceOf[T]
          case _ =>
            throw new IllegalArgumentException(
              "Unsupported type of id-value: %s".format(ctag.runtimeClass)
            )
        }
        () => id
      },
      (v: Box[T]) => v.toString,
      (key: String, e: Exception) => {
        "Cannot parse parameter %s as ixias.model.EntityId.Id\n %s".format(key, e)
      }
    )
  }

  // -- [ QueryStringBindable ] ------------------------------------------------
  /** For ixias.model.EntityId.Id
    */
  def queryStringBindableBoxId[T <: EntityId.Id](implicit
    ctag: reflect.ClassTag[T]
  ): QueryStringBindable[Box[T]] = {
    new QueryStringBindable.Parsing[Box[T]](
      (s: String) => {
        val id = ctag.runtimeClass match {
          case x if classOf[Int].isAssignableFrom(x)    => EntityId.IdLong(s.toInt).asInstanceOf[T]
          case x if classOf[Long].isAssignableFrom(x)   => EntityId.IdLong(s.toLong).asInstanceOf[T]
          case x if classOf[String].isAssignableFrom(x) => EntityId.IdString(s).asInstanceOf[T]
          case _ =>
            throw new IllegalArgumentException(
              "Unsupported type of id-value: %s".format(ctag.runtimeClass)
            )
        }
        () => id
      },
      (v: Box[T]) => v.toString(),
      (key: String, e: Exception) => {
        "Cannot parse parameter %s as ixias.model.EntityId.Id\n %s".format(key, e)
      }
    )
  }

  /** For ixias.model.EntityId.Id
    */
  def queryStringBindableBoxCsvId[T <: EntityId.Id](implicit
    ctag: reflect.ClassTag[T]
  ): QueryStringBindable[BoxCsv[T]] = {
    new QueryStringBindable.Parsing[BoxCsv[T]](
      (s: String) => {
        val ids: Seq[T] = s
          .split(",")
          .toIndexedSeq
          .map(tok =>
            ctag.runtimeClass match {
              case x if classOf[Int].isAssignableFrom(x)    => EntityId.IdLong(tok.toInt).asInstanceOf[T]
              case x if classOf[Long].isAssignableFrom(x)   => EntityId.IdLong(tok.toLong).asInstanceOf[T]
              case x if classOf[String].isAssignableFrom(x) => EntityId.IdString(tok).asInstanceOf[T]
              case _ =>
                throw new IllegalArgumentException(
                  "Unsupported type of id-value: %s".format(ctag.runtimeClass)
                )
            }
          )
        () => ids
      },
      (v: BoxCsv[T]) => v().map(_.toString).mkString(","),
      (key: String, e: Exception) => {
        "Cannot parse parameter %s as ixias.model.EntityId.Id\n %s".format(key, e)
      }
    )
  }
}
