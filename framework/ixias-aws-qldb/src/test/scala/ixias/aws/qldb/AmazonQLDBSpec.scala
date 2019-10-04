/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.aws.qldb

import org.specs2.mutable._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.libs.json.{ Json, Reads }
import ixias.persistence.model.DataSourceName

/** Table Data Model */
sealed case class JsValueHogeHoge(
  a: Int,
  b: String
)
object JsValueHogeHoge {
  implicit val writes: Reads[JsValueHogeHoge] = Json.reads[JsValueHogeHoge]
}

/** Test Specs */
class AmazonQLDBSpec extends Specification {
  "AmazonQLDBSpec" should {
    "Create Session for AmazonQLDB" in {
      implicit val dsn = DataSourceName("ixias.aws.qldb://ledger/test")
      val f = for {
        backend  <- AmazonQLDBBackend.getDatabase
        session   = backend.underlying
      } yield {
        import collection.JavaConverters._
        // session.execute(txn => {
        //   println("--[ Insert Test ] ---------------------")
        //   import com.amazon.ion.system.IonSystemBuilder
        //   val system = IonSystemBuilder.standard().build()
        //   val reader = system.singleValue("""{ "a": 2000, "b": "fugafuga" }""")
        //   scala.util.Try {
        //     txn.execute("INSERT INTO hogehoge ?", Seq(reader).asJava)
        //   }
        // })
        session.execute(txn => {
          println("--[ Select Test ] ---------------------")
          import com.amazon.ion.IonValue
          import com.amazon.ion.system.IonTextWriterBuilder
          val result = txn.execute("SELECT * FROM hogehoge WHERE a IN (1000, 2000)")
          val rows   = new java.util.ArrayList[IonValue]()
          result.iterator().forEachRemaining(row => rows.add(row))
          rows.asScala.map((v: IonValue) => {
            println(v.toString(IonTextWriterBuilder.json))
            println({
              Json.parse(
                v.toString(IonTextWriterBuilder.json)
              ).validate[JsValueHogeHoge]
            })
          })
        })
      }
      Await.ready(f, Duration.Inf)
      true must_== true
    }
  }
}
