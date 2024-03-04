/*
 * This file is part of the IxiaS service.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.slick.builder

import javax.sql.DataSource

import com.zaxxer.hikari.HikariDataSource

import slick.util.AsyncExecutor

import ixias.slick.jdbc.MySQLProfile.api._

trait DatabaseBuilder {

  private def buildAsyncExecutor(maximumPoolSize: Int) = AsyncExecutor(
    name           = "AsyncExecutor.ixias",
    minThreads     = maximumPoolSize,
    maxThreads     = maximumPoolSize,
    queueSize      = 1000,
    maxConnections = maximumPoolSize
  )

  def fromHikariDataSource(dataSource: HikariDataSource): Database = {
    val asyncExecutor = buildAsyncExecutor(dataSource.getMaximumPoolSize)
    Database.forDataSource(dataSource, Some(dataSource.getMaximumPoolSize), asyncExecutor)
  }

  def fromDataSource(dataSource: DataSource): Database =
    Database.forDataSource(dataSource, None)
}

object DatabaseBuilder extends DatabaseBuilder
