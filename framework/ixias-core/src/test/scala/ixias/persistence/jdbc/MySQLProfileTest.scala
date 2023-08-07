package ixias.persistence.jdbc

import java.time.LocalDateTime

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

import org.specs2.mutable.Specification
import org.specs2.specification.AfterSpec

import slick.jdbc.JdbcProfile

import ixias.model._
import ixias.persistence.model.Table
import ixias.persistence.SlickRepository

class MySQLProfileTest extends Specification with AfterSpec {

  def afterSpec = step {
    val result = repository.delete()
    Await.ready(result, Duration.Inf)
    result
  }

  implicit val driver    = slick.jdbc.MySQLProfile
  private val repository = new DateAndTimeTypesRepository

  private val testDateTime = LocalDateTime.parse("2023-08-07T19:32:35")

  "MySQLProfile Date and Time types Test" should {
    "LocalDateTime is the same before and after storing in DB." in {
      val mode = DateAndTimeTypes(None, testDateTime, testDateTime).toWithNoId
      val result = for {
        id    <- repository.add(mode)
        model <- repository.get(id)
      } yield {
        model.exists(_.v.createdAt == testDateTime)
      }
      Await.ready(result, Duration.Inf)
      result
    }

    "LocalDateTime is different before and after storing in DB." in {
      val failedDateTime = LocalDateTime.parse("2023-08-07T19:32:34.508")
      val mode = DateAndTimeTypes(None, failedDateTime, failedDateTime).toWithNoId
      val result = for {
        id <- repository.add(mode)
        model <- repository.get(id)
      } yield {
        model.exists(_.v.createdAt != failedDateTime)
      }
      Await.ready(result, Duration.Inf)
      result
    }
  }
}

import DateAndTimeTypes.Id
case class DateAndTimeTypes(
  id: Option[Id],
  updatedAt: LocalDateTime,
  createdAt: LocalDateTime
) extends EntityModel[Id]

object DateAndTimeTypes {

  val Id = the[Identity[Id]]
  type Id = Long @@ DateAndTimeTypes
}

case class DateAndTimeTypesTable[P <: JdbcProfile]()(
  implicit val driver: P
) extends Table[DateAndTimeTypes, P] {

  import api._

  // --[ DNS ] -----------------------------------------------------------------
  lazy val dsn = Map(
    "master" -> DataSourceName("ixias.db.mysql://master/test"),
    "slave" -> DataSourceName("ixias.db.mysql://slave/test")
  )

  class Query extends BasicQuery(new Table(_)) {}
  lazy val query = new Query

  class Table(tag: Tag) extends BasicTable(tag, "test") {
    def id = column[Id]("id", O.AutoInc, O.PrimaryKey)
    def updatedAt = column[LocalDateTime]("updated_at")
    def createdAt = column[LocalDateTime]("created_at")

    def * = (id.?, updatedAt, createdAt) <> (
      (DateAndTimeTypes.apply   _).tupled,
      DateAndTimeTypes.unapply
    )
  }
}

class DateAndTimeTypesRepository[P <: JdbcProfile]()(
  implicit val driver: P
) extends SlickRepository[DateAndTimeTypes.Id, DateAndTimeTypes, P] {
  import api._

  private val table = DateAndTimeTypesTable()

  override def get(id: Id): Future[Option[EntityEmbeddedId]] =
    RunDBAction(table, "slave") {
      _.filter(_.id === id).result.headOption
    }

  override def add(entity: EntityWithNoId): Future[Id] =
    RunDBAction(table) { slick =>
      slick returning slick.map(_.id) += entity.v
    }

  override def update(entity: EntityEmbeddedId): Future[Option[EntityEmbeddedId]] =
    RunDBAction(table) { slick =>
      val row = slick.filter(_.id === entity.id)
      for {
        old <- row.result.headOption
        _ <- old match {
          case None => slick += entity.v
          case Some(_) => row.update(entity.v)
        }
      } yield old
    }

  def delete(): Future[Int] =
    RunDBAction(table)(_.delete)

  @deprecated("unsupported operation", "")
  def remove(uid: Id): Future[Option[EntityEmbeddedId]] =
    Future.failed(new UnsupportedOperationException)
}