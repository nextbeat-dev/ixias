package ixias.aws.s3

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.Duration

import com.zaxxer.hikari.HikariDataSource
import munit.FunSuite

import ixias.slick.builder.{ DatabaseBuilder, HikariConfigBuilder }
import ixias.aws.s3.model.File
import ixias.util.Configuration

import java.nio.file.Paths

class AmazonS3RepositoryTest extends FunSuite with AmazonS3Repository {

  // --[ Utility ]-----------------------------------------------------------------
  private def await[T](f: Future[T]): T = Await.result(f, Duration.Inf)

  // --[ Properties ]---------------------------------------------------------------
  // master
  val dataSourceNameMaster      = ixias.slick.model.DataSourceName("aws.s3://master/dummy_bucket")
  val hikariConfigBuilderMaster = HikariConfigBuilder.default(dataSourceNameMaster)
  val hikariConfigMaster        = hikariConfigBuilderMaster.build()
  hikariConfigMaster.validate()
  val dataSourceMaster          = new HikariDataSource(hikariConfigMaster)
  // slave
  val dataSourceNameSlave      = ixias.slick.model.DataSourceName("aws.s3://slave/dummy_bucket")
  val hikariConfigBuilderSlave = HikariConfigBuilder.default(dataSourceNameSlave)
  val hikariConfigSlave        = hikariConfigBuilderSlave.build()
  hikariConfigSlave.validate()
  val dataSourceSlave          = new HikariDataSource(hikariConfigSlave)
  // Database
  val master                   = DatabaseBuilder.fromHikariDataSource(dataSourceMaster)
  val slave                    = DatabaseBuilder.fromHikariDataSource(dataSourceSlave)

  // --[ Properties ]---------------------------------------------------------------
  implicit def ec:  ExecutionContext = ExecutionContext.global
  implicit val dsn: DataSourceName   = DataSourceName("aws.s3://dummy_bucket")

  // --[ Properties ]---------------------------------------------------------------
  private val path        = Paths.get("")
  private val currentPath = path.toAbsolutePath
  private val localFile   = new java.io.File(s"$currentPath/src/test/resources/file/Test.txt")

  private val fileWithNoId: File#WithNoId = File("key", "iamge/png", None).getOrElse(throw new Exception("Failed to create a File object."))
  private var fileId:       File.Id       = File.Id(0L)
  println(s"${config.get[Configuration]("aws.s3")}")

  // --[ Test ]--------------------------------------------------------------------
  test("AmazonS3Repository add Success") {
    val fid = await(add(fileWithNoId, localFile))
    fileId = fid
    assert(fid.toLong > 0L)
  }

  /**
   * test("AmazonS3Repository get Success") {
   * val fileOpt = await(get(fileId))
   * assert(fileOpt.isDefined)
   * }
   *
   * test("AmazonS3Repository getWithPresigned Success") {
   * val fileOpt = await(getWithPresigned(fileId))
   * assert(fileOpt.fold(false)(_.v.presignedUrl.isDefined))
   * }
   *
   * test("AmazonS3Repository getWithContent Success") {
   * val fileOpt = await(getWithContent(fileId))
   * assert(fileOpt.isDefined)
   * }
   *
   * test("AmazonS3Repository filterWithPresigned Success") {
   * val fileOpt = await(filterWithPresigned(Seq(fileId)))
   * assert(fileOpt.headOption.fold(false)(_.v.presignedUrl.isDefined))
   * }
   *
   * test("AmazonS3Repository addViaPresignedUrl Success") {
   * assert(await(addViaPresignedUrl(fileWithNoId))._2.nonEmpty)
   * }
   */
}
