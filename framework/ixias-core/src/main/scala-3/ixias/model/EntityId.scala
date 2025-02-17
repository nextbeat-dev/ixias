/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.model

object EntityId:

  // --[ Opaque type: Id ]-------------------------------------------------------
  /** Id must be long value or string value */
  opaque type Id = Long | String | java.util.UUID

  // --[ Opaque type: Id as Long value ]-----------------------------------------
  opaque type IdLong <: Id = Long
  object IdLong:

    /** Create Id from primitive value
     *
     * @param id  the unsigned long number
     * @return Id of the entity
     */
    def apply(id: Long): IdLong =
      require(id > -1L, "Error: Id should not be a negative number.")
      id

    // - Extension methods for IdLong
    extension (id: IdLong) def toLong: Long = id

  /** Constructors for long value */
  trait IdLongGen[K <: IdLong]:

    /** Create Id from primitive value
     *
     * @param id  the unsigned long number
     * @return Id of the entity
     */
    def apply(id: Long): K = IdLong(id).asInstanceOf[K]

  // --[ Opaque type: Id as String value ]---------------------------------------
  opaque type IdString <: Id = String
  object IdString:

    /** Create Id from primitive value
     *
     * @param id  string
     * @return Id of the entity
     */
    def apply(id: String): IdString =
      require(id.nonEmpty, "Error: Id must not be an empty string.")
      id

    // - Extension methods for IdString
    extension (id: IdString) def toString: String = id

  /** Constructors for string value */
  trait IdStringGen[K <: IdString]:

    /** Create Id from primitive value
     *
     * @param id  string
     * @return Id of the entity
     */
    def apply(id: String): K = IdString(id).asInstanceOf[K]

  // --[ Opaque type: Id as UUID value ]-----------------------------------------
  opaque type UUID <: Id = java.util.UUID
  object UUID:

    /** Create Id from primitive value
     *
     * @param id  UUID
     * @return Id of the entity
     */
    def apply(id: java.util.UUID): UUID =
      id

    // - Extension methods for UUID
    extension (id: UUID)
      def toUUID:   java.util.UUID = id
      def toString: String         = id.toString

  /** Constructors for UUID value */
  trait UUIDGen[K <: UUID]:

    /** Create Id from primitive value
     *
     * @param id  UUID
     * @return Id of the entity
     */
    def apply(id: java.util.UUID): K = UUID(id).asInstanceOf[K]

    /** Create Id from string
     *
     * @param id  string
     * @return Id of the entity
     */
    def apply(id: String): K = UUID(java.util.UUID.fromString(id)).asInstanceOf[K]
