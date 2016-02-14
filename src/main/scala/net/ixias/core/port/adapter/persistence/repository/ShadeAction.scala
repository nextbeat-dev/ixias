/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package net.ixias
package core.port.adapter.persistence.repository

import scala.util.Failure
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import core.port.adapter.persistence.model.DataSourceName

/** Provides actions */
trait ShadeAction { self: ShadeProfile[_, _] =>

  /** Run the supplied function with a database object by using pool database session. */
  object DBAction extends Action[Backend, Database] {
    def invokeBlock[A](backend: Backend, dsn: DataSourceName, block: Database => Future[A]): Future[A] =
      (for {
        db <- backend.getDatabase(dsn)
        v  <- block(db)
      } yield v) andThen {
        case Failure(ex) => logger.error("The database action failed. dsn=" + dsn.toString, ex)
      }
  }
}
