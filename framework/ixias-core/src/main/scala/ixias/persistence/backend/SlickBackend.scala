/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.persistence.backend

import scala.concurrent.Future
import scala.util.{ Success, Failure }

import com.zaxxer.hikari.{ HikariConfig, HikariDataSource }
import slick.util.AsyncExecutor
import slick.jdbc.{ JdbcProfile, JdbcBackend }
import ixias.persistence.model.DataSourceName

/** The slick backend to handle the database and session.
  */
case class SlickBackend[P <: JdbcProfile](val driver: P) extends BasicBackend[P#Backend#Database] with SlickConfig {

  /** Get a Database instance from connection pool. */
  def getDatabase(implicit dsn: DataSourceName): Future[Database] =
    SlickDatabaseContainer.getOrElseUpdate {
      (for {
        ds <- createDataSource
        db <- Future(
                driver.backend.Database.forDataSource(
                  ds,
                  Some(ds.getMaximumPoolSize),
                  AsyncExecutor(
                    name           = "AsyncExecutor.ixias",
                    minThreads     = ds.getMaximumPoolSize,
                    maxThreads     = ds.getMaximumPoolSize,
                    queueSize      = 1000,
                    maxConnections = ds.getMaximumPoolSize
                  )
                )
              )
      } yield db) andThen {
        case Success(_) => logger.info("Created a new data souce. dsn=%s".format(dsn.toString))
        case Failure(_) => logger.info("Failed to create a data souce. dsn=%s".format(dsn.toString))
      }
    }

  /** Create a JdbcDataSource from DSN (Database Source Name) */
  def createDataSource(implicit dsn: DataSourceName): Future[HikariDataSource] =
    Future.fromTry {
      for {
        driver <- getDriverClassName
        url    <- getJdbcUrl
      } yield {
        val hconf = new HikariConfig()
        hconf.setDriverClassName(driver)
        hconf.setJdbcUrl(url)
        hconf.setPoolName(dsn.toString)
        hconf.addDataSourceProperty("useSSL", false)

        // Optional properties.
        getUserName map hconf.setUsername
        getPassword map hconf.setPassword
        getHostSpecReadOnly map hconf.setReadOnly
        getHostSpecMinIdle map hconf.setMinimumIdle
        getHostSpecMaxPoolSize map hconf.setMaximumPoolSize
        getHostSpecConnectionTimeout map hconf.setConnectionTimeout
        getHostSpecIdleTimeout map hconf.setIdleTimeout
        new HikariDataSource(hconf)
      }
    }
}

/** Manage data sources associated with DSN */
object SlickDatabaseContainer extends BasicDatabaseContainer[JdbcBackend#Database]
