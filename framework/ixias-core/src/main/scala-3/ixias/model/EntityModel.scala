/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.model

/** Abstract type to represent domain model */
trait EntityModel:

  /** Identity of the entity */
  type Id <: EntityId.Id

  /** Accessor to identity */
  val id: Option[Id]

  /** Wrap domain model in `Entity` datatype */
  def toWithNoId:   EntityWithNoId[this.type]   = EntityWithNoId(this)
  def toEmbeddedId: EntityEmbeddedId[this.type] = EntityEmbeddedId(this)
