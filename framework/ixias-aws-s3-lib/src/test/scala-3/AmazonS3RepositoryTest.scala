package ixias.aws.s3

import java.nio.file.Paths
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.Duration

import com.mysql.cj.jdbc.MysqlDataSource
import munit.FunSuite

import ixias.slick.builder.DatabaseBuilder
import ixias.slick.reader.DatabaseConfigReader
import ixias.aws.s3.model.File

/** Before running the tests, create an S3 bucket in LocalStack. */
class AmazonS3RepositoryTest extends FunSuite with AmazonS3Repository {

  // --[ Utility ]-----------------------------------------------------------------
  private def await[T](f: Future[T]): T = Await.result(f, Duration.Inf)

  // --[ Properties ]---------------------------------------------------------------
  // Database Configuration
  val dbConfReaderMaster = DBConfReader(ixias.slick.model.DataSourceName("aws.s3://master/dummy_bucket"))
  val dbConfReaderSlave  = DBConfReader(ixias.slick.model.DataSourceName("aws.s3://slave/dummy_bucket"))
  // Master
  val mysqlDSMaster = new MysqlDataSource()
  mysqlDSMaster.setURL(dbConfReaderMaster.URL)
  mysqlDSMaster.setUser(dbConfReaderMaster.USER)
  mysqlDSMaster.setPassword(dbConfReaderMaster.PASS)
  // Slave
  val mysqlDSSlave = new MysqlDataSource()
  mysqlDSSlave.setURL(dbConfReaderSlave.URL)
  mysqlDSSlave.setUser(dbConfReaderSlave.USER)
  mysqlDSSlave.setPassword(dbConfReaderSlave.PASS)
  // Database
  override val master = DatabaseBuilder.fromDataSource(mysqlDSMaster)
  override val slave  = DatabaseBuilder.fromDataSource(mysqlDSSlave)

  // --[ Properties ]---------------------------------------------------------------
  implicit def ec:  ExecutionContext = ExecutionContext.global
  implicit val dsn: DataSourceName   = DataSourceName("aws.s3://dummy_bucket")

  // --[ Properties ]---------------------------------------------------------------
  private val path        = Paths.get("")
  private val currentPath = path.toAbsolutePath
  private val localFile   = new java.io.File(s"$currentPath/src/test/resources/file/Test.txt")

  private val fileWithNoId: File.WithNoId =
    File("key", "iamge/png", None).getOrElse(throw new Exception("Failed to create a File object."))
  private var fileId: File.Id = File.Id(0L)

  // --[ Test ]--------------------------------------------------------------------
  test("AmazonS3Repository add Success") {
    val fid = await(add(fileWithNoId, localFile))
    fileId = fid
    assert(fileId.toLong > 0L)
  }

  test("AmazonS3Repository get Success") {
    val fileOpt = await(get(fileId))
    assert(fileOpt.isDefined)
  }

  test("AmazonS3Repository getWithPresigned Success") {
    val fileOpt = await(getWithPresigned(fileId))
    assert(fileOpt.fold(false)(_.v.presignedUrl.isDefined))
  }

  test("AmazonS3Repository getWithContent Success") {
    val fileOpt = await(getWithContent(fileId))
    assert(fileOpt.isDefined)
  }

  test("AmazonS3Repository filter Success") {
    val fileSeq = await(filter(Seq(fileId)))
    assert(fileSeq.nonEmpty && fileSeq.forall(_.v.presignedUrl.isEmpty))
  }

  test("AmazonS3Repository filterWithPresigned Success") {
    val fileSeq = await(filterWithPresigned(Seq(fileId)))
    assert(fileSeq.nonEmpty && fileSeq.forall(_.v.presignedUrl.isDefined))
  }

  test("AmazonS3Repository addViaPresignedUrl Success") {
    assert(await(addViaPresignedUrl(fileWithNoId))._2.nonEmpty)
  }

  test("AmazonS3Repository update Success") {
    val jpeg = "image/jpeg"

    val oldFileOpt = await(get(fileId))
    assert(oldFileOpt.isDefined)

    val updatedFile = oldFileOpt.get.map(_.copy(typedef = jpeg))
    await(update(updatedFile, localFile))

    val updatedFileOpt = await(get(fileId))
    assert(updatedFileOpt.fold(false)(_.v.typedef == jpeg))
  }

  test("AmazonS3Repository updateViaPresignedUrl Success") {
    val gif = "image/gif"

    val oldFileOpt = await(get(fileId))
    assert(oldFileOpt.isDefined)

    val updatedFile = oldFileOpt.get.map(_.copy(typedef = gif))
    val _oldFileOpt = await(updateViaPresignedUrl(updatedFile))
    assert(_oldFileOpt._2.nonEmpty)

    val updatedFileOpt = await(get(fileId))
    assert(updatedFileOpt.fold(false)(_.v.typedef == gif))
  }

  test("AmazonS3Repository updateImageSize Success") {
    val imageSize = Some(File.ImageSize(100, 100))

    val oldFileOpt = await(get(fileId))
    assert(oldFileOpt.isDefined)

    val updatedFile = oldFileOpt.get.map(_.copy(imageSize = imageSize))
    await(update(updatedFile, localFile))

    val updatedFileOpt = await(get(fileId))
    assert(updatedFileOpt.fold(false)(_.v.imageSize == imageSize))
  }

  test("AmazonS3Repository remove Success") {
    val fileOpt = await(get(fileId))
    assert(fileOpt.isDefined)

    await(remove(fileId))

    val removedFileOpt = await(get(fileId))
    assert(removedFileOpt.isEmpty)

    fileOpt.foreach { file =>
      assert(client.load(file.v.bucket, file.v.key).isSuccess)
    }
  }

  test("AmazonS3Repository bulkRemove Success") {
    val fid  = await(add(fileWithNoId, localFile))
    val fid2 = await(add(fileWithNoId, localFile))
    val fids = Seq(fid, fid2)

    val fileSeq = await(filter(fids))
    assert(fileSeq.forall(file => fids.contains(file.id)))

    await(bulkRemove(fids))
    assert(await(filter(fids)).isEmpty)

    fileSeq.foreach { file =>
      assert(client.load(file.v.bucket, file.v.key).isSuccess)
    }
  }

  test("AmazonS3Repository erase Success") {
    val fid = await(add(fileWithNoId, localFile))

    val fileOpt = await(get(fid))
    assert(fileOpt.isDefined)

    await(erase(fid))

    val erasedFileOpt = await(get(fid))
    assert(erasedFileOpt.isEmpty)

    fileOpt.foreach { file =>
      assert(client.load(file.v.bucket, file.v.key).isFailure)
    }
  }

  test("AmazonS3Repository bulkErase Success") {
    val fid  = await(add(fileWithNoId, localFile))
    val fid2 = await(add(fileWithNoId, localFile))
    val fids = Seq(fid, fid2)

    val fileSeq = await(filter(fids))
    assert(fileSeq.forall(file => fids.contains(file.id)))

    await(bulkErase(fids))
    assert(await(filter(fids)).isEmpty)

    fileSeq.foreach { file =>
      assert(client.load(file.v.bucket, file.v.key).isFailure)
    }
  }
}

case class DBConfReader(dsn: ixias.slick.model.DataSourceName) extends DatabaseConfigReader {
  lazy val URL: String = readValue(_.get[Option[String]]("jdbc_url"))(dsn)
    .getOrElse(throw new IllegalArgumentException("No JDBC URL is set."))
  lazy val USER: String = getUserName(dsn)
    .getOrElse(throw new IllegalArgumentException("No database user name is set."))
  lazy val PASS: String = getPassword(dsn)
    .getOrElse(throw new IllegalArgumentException("No database password is set."))
}
