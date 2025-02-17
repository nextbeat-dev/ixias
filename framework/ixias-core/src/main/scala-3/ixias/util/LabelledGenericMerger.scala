/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.util

import scala.deriving.Mirror

/** Merger: model to model with using `LabelledGeneric`
  */
trait LabelledGenericMerger[T, U]:
  def merge(data1: T, data2: U, hard: Boolean = false): T

object LabelledGenericMerger:
  inline def apply[T, U](using mT: Mirror.ProductOf[T], mU: Mirror.ProductOf[U]): LabelledGenericMerger[T, U] =
    new LabelledGenericMerger[T, U]:

      def merge(data1: T, data2: U, hard: Boolean): T =
        val mergedValues = productMerge(
          productToList(data1),
          productToList(data2),
          hard
        )
        val productAsTuple = listToTuple[Tuple](mergedValues)
        mT.fromProduct(productAsTuple)

      private def productToList[A](a: A)(using m: Mirror.ProductOf[A]) =
        a.asInstanceOf[Product].productIterator.toList

      private def productMerge(a: List[Any], b: List[Any], hard: Boolean): List[Any] =
        a.zipAll(b, None, None).map {
          case (left, right) =>
            (left, right) match
              case (Some(lv), Some(rv)) => if hard then Some(rv) else Some(rv)
              case (None, Some(rv))     => Some(rv)
              case (Some(lv), None)     => if hard then None else Some(lv)
              case _                    => if hard then right else if right != null then right else left
        }

      private def listToTuple[T](list: List[Any]): T =
        Tuple.fromArray(list.toArray).asInstanceOf[T]
