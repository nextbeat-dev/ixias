/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.model

/** Wrapper class for domain model.
 *
 *  @tparam M  the type of a domain model
 */
sealed abstract class Entity[+M <: EntityModel]:

  /** @param v  the wrapped data of the domain model */
  val v: M

  /** Get idnetity of this entity.
   *
   *  @return  id of this entity if it has.
   */
  def id: v.Id

  /** Get whether this entity has an identity.
   *
   *  @return  whether to have an identity.
   */
  def hasId: Boolean

/**
 * Entity class which doesn't have an identity
 *
 * @tparam M  the type of a domain model
 * @param  v  the wrapped data of the domain model
 */
case class EntityWithNoId[+M <: EntityModel](v: M) extends Entity[M]:
  @deprecated("unsupported operation", "1.0.0")
  def id    = throw new UnsupportedOperationException
  def hasId = false

/**
 * Entity class which has an identity
 *
 * @tparam M  the type of a domain model
 * @param  v  the wrapped data of the domain model
 */
case class EntityEmbeddedId[+M <: EntityModel](v: M) extends Entity[M]:
  def id    = v.id.get
  def hasId = true
