/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.slick.lifted

import scala.language.implicitConversions

import slick.ast.{Library, TypedType}
import slick.lifted.{FunctionSymbolExtensionMethods, Query, Rep}

import ixias.model.*

final case class SlickQueryTransformer[E, U, C[_]](self: Query[E, U, C]) extends AnyVal {
  def seek(cursor: Cursor): Query[E, U, C] =
    cursor.limit match {
      case None        => if (0 < cursor.offset) self.drop(cursor.offset) else self
      case Some(limit) => self.drop(cursor.offset).take(limit)
    }
}

final case class SlickQueryTransformerId[T <: EntityId.Id, U, C[_]](
  self: Query[Rep[T], U, C]
) extends AnyVal {
  def distinctLength(implicit tm: TypedType[Int]): Rep[Int] =
    FunctionSymbolExtensionMethods
      .functionSymbolExtensionMethods(Library.CountDistinct)
      .column(self.toNode)
}

trait SlickQueryOps {
  implicit def toQueryTransformer[E, U, C[_]](a: Query[E, U, C]): SlickQueryTransformer[E, U, C] =
    SlickQueryTransformer(a)

  implicit def toQueryTransformerId[T <: EntityId.Id, U, C[_]](
    a: Query[Rep[T], U, Seq]
  ): SlickQueryTransformerId[T, U, Seq] = SlickQueryTransformerId(a)
}
