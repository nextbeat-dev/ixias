/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.slick.lifted

import ixias.model._

import scala.language.implicitConversions

trait ConverterOps {
  // for EntityModel
  implicit def toModelToEntity[M <: EntityModel](m: M): EntityEmbeddedId[M] =
    EntityEmbeddedId[M](m)

  // for Seq[EntityModel]
  implicit def toModelToEntitySeq[M <: EntityModel](m: Seq[M]): Seq[EntityEmbeddedId[M]] =
    m.map(EntityEmbeddedId[M])

  // for Option[EntityModel]
  implicit def toModelToEntityOpt[M <: EntityModel](m: Option[M]): Option[EntityEmbeddedId[M]] =
    m.map(EntityEmbeddedId[M])

  implicit def convert[A, B](o: A)(implicit conv: Converter[A, B]): B = conv.convert(o)
}
