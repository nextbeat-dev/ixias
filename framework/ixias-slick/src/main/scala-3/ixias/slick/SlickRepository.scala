/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.slick

import ixias.model._
import ixias.slick.lifted._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

trait SlickRepository[M <: EntityModel] extends ConverterOps with SlickQueryOps {
  implicit def ec: ExecutionContext

  /** The type of entity when it has not id. */
  type EntityWithNoId = ixias.model.EntityWithNoId[M]

  /** The type of entity when it has embedded id */
  type EntityEmbeddedId = ixias.model.EntityEmbeddedId[M]

  implicit def toFutureModelToEntityOpt(m: Future[Option[M]])(implicit
    conv: Option[M] => Option[EntityEmbeddedId]
  ): Future[Option[EntityEmbeddedId]] =
    m.map(conv)

  implicit def toFutureModelToEntitySeq(m: Future[Seq[M]])(implicit
    conv: Seq[M] => Seq[EntityEmbeddedId]
  ): Future[Seq[EntityEmbeddedId]] =
    m.map(conv)
}
