/*
 * This file is part of the IxiaS service.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.slick.reader

import scala.util.Try
import scala.jdk.CollectionConverters._
import scala.concurrent.duration.Duration

import ixias.util.Configuration
import ixias.slick.model.DataSourceName

trait DatabaseConfigReader {

  /** The section format */
  protected val SECTION_HOST_SPEC = """hostspec.%s"""

  /** The keys of configuration */
  protected val USERNAME           = "username"
  protected val PASSWORD           = "password"
  protected val DRIVER_CLASS_NAME  = "driver_class_name"
  protected val HOSTS              = "hosts"
  protected val DATABASE           = "database"
  protected val SCHEMA             = "schema"
  protected val READONLY           = "readonly"
  protected val MIN_IDLE           = "min_idle"
  protected val MAX_POOL_SIZE      = "max_pool_size"
  protected val CONNECTION_TIMEOUT = "connection_timeout"
  protected val IDLE_TIMEOUT       = "idle_timeout"

  /** The configuration */
  protected val config: Configuration = Configuration()

  /**
   * Get a value by specified key.
   */
  final protected def readValue[A](f: Configuration => Option[A])(implicit dsn: DataSourceName): Option[A] =
    Seq(
      dsn.path + "." + dsn.database + "." + SECTION_HOST_SPEC.format(dsn.hostspec),
      dsn.path + "." + dsn.database,
      dsn.path + "." + SECTION_HOST_SPEC.format(dsn.hostspec),
      dsn.path
    ).foldLeft[Option[A]](None) {
      case (prev, path) => prev.orElse {
        config.get[Option[Configuration]](path).flatMap(f)
      }
    }

  // --[ Methods ]--------------------------------------------------------------

  /** Get the username used for DataSource */
  protected def getUserName(implicit dsn: DataSourceName): Option[String] =
    readValue(_.get[Option[String]](USERNAME))

  /** Get the password used for DataSource */
  protected def getPassword(implicit dsn: DataSourceName): Option[String] =
    readValue(_.get[Option[String]](PASSWORD))

  /** Get the flag for connection in read-only mode. */
  protected def getHostSpecReadOnly(implicit dsn: DataSourceName): Option[Boolean] =
    readValue(_.get[Option[Boolean]](READONLY))

  // --[ Methods ]--------------------------------------------------------------

  /** Get the JDBC driver class name. */
  protected def getDriverClassName(implicit dsn: DataSourceName): Try[String] =
    Try(readValue(_.get[Option[String]](DRIVER_CLASS_NAME)).get)

  /** Get the database name. */
  protected def getDatabaseName(implicit dsn: DataSourceName): Try[String] =
    Try(readValue(_.get[Option[String]](DATABASE)).get)

  /** Get the schema name. */
  protected def getSchemaName(implicit dsn: DataSourceName): Try[String] =
    Try(readValue(_.get[Option[String]](SCHEMA)).get)

  /** Get host list to connect to database. */
  protected def getHosts(implicit dsn: DataSourceName): Try[Seq[String]] = {
    val path = dsn.path + '.' + dsn.database + '.' + SECTION_HOST_SPEC.format(dsn.hostspec)
    val section = config.get[Configuration](path).underlying
    val opt = section.getAnyRef(HOSTS) match {
      case v: String => Seq(v)
      case v: java.util.List[_] => v.asScala.toList.map(_.toString)
      case _ => throw new Exception(s"""Illegal value type of host setting. { path: $dsn }""")
    }
    Try(opt)
  }

  protected def getHostSpecMinIdle(implicit dsn: DataSourceName): Option[Int] =
    readValue(_.get[Option[Int]](MIN_IDLE))

  protected def getHostSpecMaxPoolSize(implicit dsn: DataSourceName): Option[Int] =
    readValue(_.get[Option[Int]](MAX_POOL_SIZE))

  protected def getHostSpecConnectionTimeout(implicit dsn: DataSourceName): Option[Long] =
    readValue(_.get[Option[Duration]](CONNECTION_TIMEOUT).map(_.toMillis))

  protected def getHostSpecIdleTimeout(implicit dsn: DataSourceName): Option[Long] =
    readValue(_.get[Option[Duration]](IDLE_TIMEOUT).map(_.toMillis))
}
