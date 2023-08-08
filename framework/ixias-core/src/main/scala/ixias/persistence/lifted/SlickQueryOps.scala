/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.lifted

import slick.ast.{ TypedType, Library }
import slick.lifted.{ Rep, Query, FunctionSymbolExtensionMethods }
import ixias.persistence.model.Cursor

import scala.language.higherKinds
import scala.language.implicitConversions

final case class SlickQueryTransformer[E, U, C[_]](val self: Query[E, U, C]) extends AnyVal {
  def seek(cursor: Cursor): Query[E, U, C] =
    cursor.limit match {
      case None        => if (0 < cursor.offset) self.drop(cursor.offset) else self
      case Some(limit) => self.drop(cursor.offset).take(limit)
    }
}

final case class SlickQueryTransformerId[T <: ixias.model.@@[_, _], U, C[_]](
  val self: Query[Rep[T], U, C]
) extends AnyVal {
  def distinctLength(implicit tm: TypedType[Int]): Rep[Int] =
    FunctionSymbolExtensionMethods
      .functionSymbolExtensionMethods(Library.CountDistinct)
      .column(self.toNode)
}

trait SlickQueryOps {
  implicit def toQueryTransformer[E, U, C[_]](a: Query[E, U, C])                                  = SlickQueryTransformer(a)
  implicit def toQueryTransformerId[T <: ixias.model.@@[_, _], U, C[_]](a: Query[Rep[T], U, Seq]) = SlickQueryTransformerId(a)
}
