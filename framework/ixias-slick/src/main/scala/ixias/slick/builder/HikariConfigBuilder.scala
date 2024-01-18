/*
 * This file is part of the IxiaS service.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.slick.builder

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.Duration

import com.zaxxer.hikari.HikariConfig

import ixias.slick.model.DataSourceName
import ixias.slick.reader.DatabaseConfigReader

/** Build the Configuration of HikariCP.
 */
trait HikariConfigBuilder extends DatabaseConfigReader {

  implicit def dsn: DataSourceName

  /** List of keys to retrieve from conf file. */
  private val CATALOG                     = "catalog"
  private val LEAK_DETECTION_THRESHOLD    = "leak_detection_threshold"
  private val MAXIMUM_POOL_SIZE           = "maximum_pool_size"
  private val MAX_LIFETIME                = "max_lifetime"
  private val MINIMUM_IDLE                = "minimum_idle"
  private val POOL_NAME                   = "pool_name"
  private val VALIDATION_TIMEOUT          = "validation_timeout"
  private val ALLOW_POOL_SUSPENSION       = "allow_pool_suspension"
  private val AUTO_COMMIT                 = "auto_commit"
  private val CONNECTION_INIT_SQL         = "connection_init_sql"
  private val CONNECTION_TEST_QUERY       = "connection_test_query"
  private val DATA_SOURCE_CLASSNAME       = "data_source_classname"
  private val DATASOURCE_JNDI             = "datasource_jndi"
  private val INITIALIZATION_FAIL_TIMEOUT = "initialization_fail_timeout"
  private val ISOLATE_INTERNAL_QUERIES    = "isolate_internal_queries"
  private val JDBC_URL                    = "jdbc_url"
  private val REGISTER_MBEANS             = "register_mbeans"
  private val TRANSACTION_ISOLATION       = "transaction_isolation"

  /** Number of application cores */
  private val maxCore: Int = Runtime.getRuntime.availableProcessors()

  /** Method to retrieve catalog information from the conf file. */
  private def getCatalog: Option[String] =
    readValue(_.get[Option[String]](CATALOG))

  /** Method to retrieve connection timeout information from the conf file. */
  private def getConnectionTimeout: Option[Duration] =
    readValue(_.get[Option[Duration]](CONNECTION_TIMEOUT))

  /** Method to retrieve idle timeout information from the conf file. */
  private def getIdleTimeout: Option[Duration] =
    readValue(_.get[Option[Duration]](IDLE_TIMEOUT))

  /** Method to retrieve leak detection threshold information from the conf file. */
  private def getLeakDetectionThreshold: Option[Duration] =
    readValue(_.get[Option[Duration]](LEAK_DETECTION_THRESHOLD))

  /** Method to retrieve maximum pool size information from the conf file. */
  private def getMaximumPoolSize: Option[Int] =
    readValue(_.get[Option[Int]](MAXIMUM_POOL_SIZE))

  /** Method to retrieve max life time information from the conf file. */
  private def getMaxLifetime: Option[Duration] =
    readValue(_.get[Option[Duration]](MAX_LIFETIME))

  /** Method to retrieve minimum idle information from the conf file. */
  private def getMinimumIdle: Option[Int] =
    readValue(_.get[Option[Int]](MINIMUM_IDLE))

  /** Method to retrieve pool name information from the conf file. */
  private def getPoolName: Option[String] =
    readValue(_.get[Option[String]](POOL_NAME))

  /** Method to retrieve validation timeout information from the conf file. */
  private def getValidationTimeout: Option[Duration] =
    readValue(_.get[Option[Duration]](VALIDATION_TIMEOUT))

  /** Method to retrieve allow pool suspension information from the conf file. */
  private def getAllowPoolSuspension: Option[Boolean] =
    readValue(_.get[Option[Boolean]](ALLOW_POOL_SUSPENSION))

  /** Method to retrieve auto commit information from the conf file. */
  private def getAutoCommit: Option[Boolean] =
    readValue(_.get[Option[Boolean]](AUTO_COMMIT))

  /** Method to retrieve connection init sql information from the conf file. */
  private def getConnectionInitSql: Option[String] =
    readValue(_.get[Option[String]](CONNECTION_INIT_SQL))

  /** Method to retrieve connection test query information from the conf file. */
  private def getConnectionTestQuery: Option[String] =
    readValue(_.get[Option[String]](CONNECTION_TEST_QUERY))

  /** Method to retrieve data source class name information from the conf file. */
  private def getDataSourceClassname: Option[String] =
    readValue(_.get[Option[String]](DATA_SOURCE_CLASSNAME))

  /** Method to retrieve data source jndi information from the conf file. */
  private def getDatasourceJndi: Option[String] =
    readValue(_.get[Option[String]](DATASOURCE_JNDI))

  /** Method to retrieve initialization fail time out information from the conf file. */
  private def getInitializationFailTimeout: Option[Duration] =
    readValue(_.get[Option[Duration]](INITIALIZATION_FAIL_TIMEOUT))

  /** Method to retrieve isolate internal queries information from the conf file. */
  private def getIsolateInternalQueries: Option[Boolean] =
    readValue(_.get[Option[Boolean]](ISOLATE_INTERNAL_QUERIES))

  /** Method to retrieve jdbc url information from the conf file. */
  private def getJdbcUrl: Option[String] =
    readValue(_.get[Option[String]](JDBC_URL))

  /** Method to retrieve readonly information from the conf file. */
  private def getReadonly: Option[Boolean] =
    readValue(_.get[Option[Boolean]](READONLY))

  /** Method to retrieve register mbeans information from the conf file. */
  private def getRegisterMbeans: Option[Boolean] =
    readValue(_.get[Option[Boolean]](REGISTER_MBEANS))

  /** Method to retrieve schema information from the conf file. */
  private def getSchema: Option[String] =
    readValue(_.get[Option[String]](SCHEMA))

  /** Method to retrieve user name information from the conf file. */
  protected def getUserName: Option[String] =
    readValue(_.get[Option[String]](USERNAME))

  /** Method to retrieve password information from the conf file. */
  protected def getPassword: Option[String] =
    readValue(_.get[Option[String]](PASSWORD))

  /** Method to retrieve driver class name information from the conf file. */
  protected def getDriverClassName: Option[String] =
    readValue(_.get[Option[String]](DRIVER_CLASS_NAME))

  /** Method to retrieve transaction isolation information from the conf file. */
  protected def getTransactionIsolation: Option[String] =
    readValue(_.get[Option[String]](TRANSACTION_ISOLATION)).map { v =>
      if (v == "TRANSACTION_NONE" || v == "TRANSACTION_READ_UNCOMMITTED" || v == "TRANSACTION_READ_COMMITTED" || v == "TRANSACTION_REPEATABLE_READ" || v == "TRANSACTION_SERIALIZABLE") {
        v
      } else {
        throw new IllegalArgumentException(
          "TransactionIsolation must be TRANSACTION_NONE,TRANSACTION_READ_UNCOMMITTED,TRANSACTION_READ_COMMITTED,TRANSACTION_REPEATABLE_READ,TRANSACTION_SERIALIZABLE."
        )
      }
    }

  /** List of variables predefined as default settings. */
  val connectionTimeout:      Long    = getConnectionTimeout.getOrElse(Duration(30, TimeUnit.SECONDS)).toMillis
  val idleTimeout:            Long    = getIdleTimeout.getOrElse(Duration(10, TimeUnit.MINUTES)).toMillis
  val leakDetectionThreshold: Long    = getLeakDetectionThreshold.getOrElse(Duration.Zero).toMillis
  val maximumPoolSize:        Int     = getMaximumPoolSize.getOrElse(maxCore * 2)
  val maxLifetime:            Long    = getMaxLifetime.getOrElse(Duration(30, TimeUnit.MINUTES)).toMillis
  val minimumIdle:            Int     = getMinimumIdle.getOrElse(10)
  val validationTimeout:      Long    = getValidationTimeout.getOrElse(Duration(5, TimeUnit.SECONDS)).toMillis
  val allowPoolSuspension:    Boolean = getAllowPoolSuspension.getOrElse(false)
  val autoCommit:             Boolean = getAutoCommit.getOrElse(true)
  val initializationFailTimeout: Long =
    getInitializationFailTimeout.getOrElse(Duration(1, TimeUnit.MILLISECONDS)).toMillis
  val isolateInternalQueries: Boolean = getIsolateInternalQueries.getOrElse(false)
  val readonly:               Boolean = getReadonly.getOrElse(false)
  val registerMbeans:         Boolean = getRegisterMbeans.getOrElse(false)

  def build(): HikariConfig = {
    val hikariConfig = new HikariConfig()

    getCatalog foreach hikariConfig.setCatalog
    hikariConfig.setConnectionTimeout(connectionTimeout)
    hikariConfig.setIdleTimeout(idleTimeout)
    hikariConfig.setLeakDetectionThreshold(leakDetectionThreshold)
    hikariConfig.setMaximumPoolSize(maximumPoolSize)
    hikariConfig.setMaxLifetime(maxLifetime)
    hikariConfig.setMinimumIdle(minimumIdle)
    hikariConfig.setValidationTimeout(validationTimeout)
    hikariConfig.setAllowPoolSuspension(allowPoolSuspension)
    hikariConfig.setAutoCommit(autoCommit)
    hikariConfig.setInitializationFailTimeout(initializationFailTimeout)
    hikariConfig.setIsolateInternalQueries(isolateInternalQueries)
    hikariConfig.setReadOnly(readonly)
    hikariConfig.setRegisterMbeans(registerMbeans)

    getPassword foreach hikariConfig.setPassword
    getPoolName foreach hikariConfig.setPoolName
    getUserName foreach hikariConfig.setUsername
    getConnectionInitSql foreach hikariConfig.setConnectionInitSql
    getConnectionTestQuery foreach hikariConfig.setConnectionTestQuery
    getDataSourceClassname foreach hikariConfig.setDataSourceClassName
    getDatasourceJndi foreach hikariConfig.setDataSourceJNDI
    getDriverClassName foreach hikariConfig.setDriverClassName
    getJdbcUrl foreach hikariConfig.setJdbcUrl
    getSchema foreach hikariConfig.setSchema
    getTransactionIsolation foreach hikariConfig.setTransactionIsolation

    hikariConfig
  }
}

object HikariConfigBuilder {

  def default(_dsn: DataSourceName): HikariConfigBuilder = new HikariConfigBuilder {
    override implicit def dsn: DataSourceName = _dsn
  }
}
