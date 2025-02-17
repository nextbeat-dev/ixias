/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.util

/** A simplified Poly2-like trait for Scala 3. */
trait Poly2:
  /** Apply operation taking any two values. */
  type Out[A, B]
  inline def apply[A, B](acc: A, x: B): Out[A, B]

/** Recursively fold a Tuple from left to right using a Poly2 operation. */
inline def foldLeft[T <: Tuple, A, P <: Poly2](t: T, acc: A, p: P): Any =
  inline t match
    case EmptyTuple    => acc
    case head *: tail =>
      val next = p.apply(acc, head)
      foldLeft(tail, next, p)

/** Polymorphic left join operation without Shapeless. */
case class PolyLeftJoinSyntax[R, P <: Poly2](underlying: R, poly2: P):
  inline def apply[T <: Tuple](haystack: T): Any =
    foldLeft(haystack, underlying, poly2)

/** Companion object for PolyLeftJoinSyntax. */
object PolyLeftJoinSyntax:

  trait Rule[R] extends Poly2:
    // Define specific logic here if needed, such as conversions or combination rules.
    // For example:
    type Out[A, B] = A // Placeholder for custom merge logic
    inline def apply[A, B](acc: A, x: B): A = acc
