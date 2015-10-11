/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core
package port.adapter.persistence
package backend

import action.IOActionContext

trait DatabaseComponent { self =>

  // --[ TypeDefs ]-------------------------------------------------------------
  type This >: this.type <: DatabaseComponent

  /** The type of database source config used by this backend. */
  type DatabaseSouceConfig <: DatabaseSouceConfigDef
  /** The type of the database souce config factory used by this backend. */
  type DatabaseSouceConfigFactory <: DatabaseSouceConfigFactoryDef
  /** The type of the context used for running Database Actions */
  type Context >: Null <: DatabaseActionContext

  // --[ Properties ]-----------------------------------------------------------
  /** The database factory */
  val DatabaseSouceConfig: DatabaseSouceConfigFactory

  // --[ DatabaseSouceConfigDef ] ----------------------------------------------
  /** A database souce config instance to which connections can be created. */
  trait DatabaseSouceConfigDef extends Serializable { this: DatabaseSouceConfig =>
  }

  // --[ DatabaseSouceConfigFactoryDef ] ---------------------------------------
  /** The database souce config factory */
  trait DatabaseSouceConfigFactoryDef { this: DatabaseSouceConfigFactory =>
  }

  /** The context object passed to database actions by the repository. */
  trait DatabaseActionContext extends IOActionContext {
  }
}
