/*
 * This file is part of the IxiaS services.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.action

import scala.util.Failure
import scala.concurrent.Future

import slick.driver.JdbcProfile
import slick.dbio.{ DBIOAction, NoStream }

import ixias.persistence.SlickProfile
import ixias.persistence.model.{ DataSourceName, Table, Converter }

trait SlickDBActionProvider[P <: JdbcProfile] { self: SlickProfile[P] =>

  /** The default using key of DSN map. */
  val DEFAULT_DSN_KEY = DataSourceName.RESERVED_NAME_MASTER

  /**
   * The Request of Invocation.
   */
  sealed case class SlickDBActionRequest[T <: Table[_, P]](
    val dsn:   DataSourceName,
    val table: T
  )

  sealed case class SlickDBAction[T <: Table[_, P]]()
      extends BasicAction[SlickDBActionRequest[T], (Database, T#Query)]
  {
    type Request       = SlickDBActionRequest[T]
    type BlockArgument = (Database, T#Query)

    /** Run block process */
    def invokeBlock[A](req: Request, block: BlockArgument => Future[A]): Future[A] =
      (for {
        db    <- backend.getDatabase(req.dsn)
        value <- block((db, req.table.query))
      } yield value) andThen {
        case Failure(ex) => logger.error(
          "The database action failed. dsn=%s".format(req.dsn.toString), ex)
      }
  }

  /**
   * The Database Acion
   */
  object SlickDBAction extends SlickDBAction {

    /** Invoke DB action block. */
    def apply[A, T <: Table[_, P]]
      (table: T, hostspec: String = DEFAULT_DSN_KEY)
      (block: ((Database, T#Query)) => Future[A]): Future[A] =
      for {
        dsn   <- Future(table.dsn.get(hostspec).get)
        value <- SlickDBAction[T].invokeBlock(SlickDBActionRequest(dsn, table), block)
      } yield value

    /** Invoke DB action block with adaption convert. */
    def adapt[A, B, T <: Table[_, P]]
      (table: T, hostspec: String = DEFAULT_DSN_KEY)
      (block: ((Database, T#Query)) => Future[A])
      (implicit conv: Converter[A, B]): Future[B] =
      for {
        dsn     <- Future(table.dsn.get(hostspec).get)
        value   <- SlickDBAction[T].invokeBlock(SlickDBActionRequest(dsn, table), block)
        adapted <- Future(conv.convert(value))
      } yield adapted
    }

  /**
   * The Database Acion
   */
  object SlickRunDBAction extends SlickDBAction {

    /** Invoke DB action block with adaption convert. */
    def apply[A, T <: Table[_, P]]
      (table: T, hostspec: String = DEFAULT_DSN_KEY)
      (action: T#Query => DBIOAction[A, NoStream, Nothing]): Future[A] =
      for {
        dsn   <- Future(table.dsn.get(hostspec).get)
        value <- SlickDBAction[T].invokeBlock(SlickDBActionRequest(dsn, table), {
          case (db, slick) => db.run(action(slick))
        })
      } yield value

    /** Invoke DB action block with adaption convert. */
    def adapt[A, B, T <: Table[_, P]]
      (table: T, hostspec: String = DEFAULT_DSN_KEY)
      (action: T#Query => DBIOAction[A, NoStream, Nothing])
      (implicit conv: Converter[A, B]): Future[B] =
      for {
        dsn    <- Future(table.dsn.get(hostspec).get)
        value  <- SlickDBAction[T].invokeBlock(SlickDBActionRequest(dsn, table), {
          case (db, slick) => db.run(action(slick))
        })
        adapted <- Future(conv.convert(value))
      } yield adapted
  }
}