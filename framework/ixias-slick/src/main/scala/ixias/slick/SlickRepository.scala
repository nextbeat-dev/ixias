/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.slick

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.implicitConversions

import ixias.model._
import ixias.slick.lifted._

trait SlickRepository[K <: @@[_, _], M <: EntityModel[K]] extends ConverterOps with SlickQueryOps {
  implicit def ec: ExecutionContext

  /** The type of entity when it has not id. */
  type EntityWithNoId = Entity[K, M, IdStatus.Empty]

  /** The type of entity when it has embedded id */
  type EntityEmbeddedId = Entity[K, M, IdStatus.Exists]

  implicit def toFutureModelToEntityOpt(m: Future[Option[M]])(implicit
    conv: Option[M] => Option[EntityEmbeddedId]
  ): Future[Option[EntityEmbeddedId]] =
    m.map(conv)

  implicit def toFutureModelToEntitySeq(m: Future[Seq[M]])(implicit
    conv: Seq[M] => Seq[EntityEmbeddedId]
  ): Future[Seq[EntityEmbeddedId]] =
    m.map(conv)
}
