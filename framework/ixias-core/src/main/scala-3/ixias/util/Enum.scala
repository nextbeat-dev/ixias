/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.util

import scala.math.pow

/** Base trait for enum types which has short value */
trait EnumStatus:
  val code: Short

/** Generator for [[EnumStatus]] */
trait EnumStatusGen[S <: EnumStatus]:

  /** Get all values of enum types.
   *
   * This method is automatically implemented when standard enumeration type is defined.
   */
  def values: Array[S]

  /** Create `EnumStatus` by its code
   *
   * @param  code  short value that represents the enum
   * @return  an enum value specified by `code`. If an enum value is not found, throw error.
   */
  def findByCode(code: Short): S = values.find(_.code == code).get

/** Base trait for enum bit flag types which has long value */
trait EnumBitFlags:
  val code: Long

/** Generator for [[EnumBitFlags]] */
trait EnumBitFlagsGen[S <: EnumBitFlags]:

  /** Get all values of enum types.
   *
   * This method is automatically implemented when standard enumeration type is defined.
   */
  def values: Array[S]

  /** Get bitset objects from numeric bitset. */
  def apply(bitset: Long): Seq[S] =
    toEnumSeq(bitset)

  /** Get bitset objects from numeric bitsets. */
  def apply(bitset: Seq[Short]): Seq[S] =
    bitset.flatMap(b => toEnumSeq(toCode(b)))

  /** Convert bitNum to BitFlag numbers */
  def toCode(bitNum: Short): Long =
    pow(2, bitNum.toDouble).toLong

  /** Calculate bitset as numeric */
  def toBitset(bitset: Seq[S]): Long =
    bitset.foldLeft(0L)((code, cur) => code | cur.code)

  /** Calculate bitset as bit flags */
  def toEnumSeq(bitset: Long): Seq[S] =
    values.toSeq.filter(p => (p.code & bitset) == p.code)

  /** Check to whether has a bit flag. */
  def hasBitFlag(bitset: Seq[S], flag: S):    Boolean = (toBitset(bitset) & flag.code) == flag.code
  def hasBitFlag(bitset: Seq[S], code: Long): Boolean = (toBitset(bitset) & code) == code
  def hasBitFlag(bitset: Long, flag:   S):    Boolean = (bitset & flag.code) == flag.code
  def hasBitFlag(bitset: Long, code:   Long): Boolean = (bitset & code) == code

  /** Set a specified bit flag. */
  def setBitFlag(bitset: Seq[S], flag: S):    Seq[S] = apply(toBitset(bitset) | flag.code)
  def setBitFlag(bitset: Seq[S], code: Long): Seq[S] = apply(toBitset(bitset) | code)
  def setBitFlag(bitset: Long, flag:   S):    Long   = bitset | flag.code
  def setBitFlag(bitset: Long, code:   Long): Long   = bitset | code
