package ixias.persistence.jdbc

import java.time._

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

  implicit val driver = slick.jdbc.MySQLProfile
  private val repository = new DateAndTimeTypesRepository

  private val testLocalDate = LocalDate.parse("2023-08-07")
  private val testLocalTime = LocalTime.parse("18:20:28")
  private val testDateTime = LocalDateTime.parse("2023-08-07T19:32:35")

  def afterSpec = step {
    val result = repository.delete()
    Await.ready(result, Duration.Inf)
    result
  }

  "MySQLProfile Date and Time types Test" should {
    "LocalDateTime is the same before and after storing in DB." in {
      val mode = DateAndTimeTypes(None, testLocalDate, testLocalTime, testDateTime, testDateTime).toWithNoId
      val result = for {
        id    <- repository.add(mode)
        model <- repository.get(id)
      } yield {
        model.exists(v =>
          v.v.createdAt == testDateTime && v.v.localDate == testLocalDate && v.v.localTime == testLocalTime
        )
      }
      Await.ready(result, Duration.Inf)
      result
    }

    "LocalDateTime is different before and after storing in DB." in {
      val failedLocalTime = LocalTime.parse("18:20:28.661")
      val failedDateTime = LocalDateTime.parse("2023-08-07T19:32:34.508")
      val mode = DateAndTimeTypes(None, testLocalDate, failedLocalTime, failedDateTime, failedDateTime).toWithNoId
      val result = for {
        id <- repository.add(mode)
        model <- repository.get(id)
      } yield {
        model.exists(v =>
          v.v.createdAt != failedDateTime && v.v.localTime != failedDateTime
        )
      }
      Await.ready(result, Duration.Inf)
      result
    }
  }
}

import DateAndTimeTypes.Id
case class DateAndTimeTypes(
  id: Option[Id],
  localDate: LocalDate,
  localTime: LocalTime,
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
    def localDate = column[LocalDate]("local_date")
    def localTime = column[LocalTime]("local_time")
    def updatedAt = column[LocalDateTime]("updated_at")
    def createdAt = column[LocalDateTime]("created_at")

    def * = (
      id.?, localDate, localTime, updatedAt, createdAt
    ) <> (
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