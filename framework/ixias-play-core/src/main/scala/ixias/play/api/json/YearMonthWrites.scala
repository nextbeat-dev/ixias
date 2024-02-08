/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.json

import java.time.YearMonth
import java.time.format.DateTimeFormatter
import play.api.libs.json.{EnvWrites, Writes}

import scala.language.implicitConversions

object YearMonthWrites extends EnvWrites {

  /** Formatting */
  implicit def DefaultYearMonthFormatter(formatter: DateTimeFormatter): YearMonthWrites.TemporalFormatter[YearMonth] =
    new TemporalFormatter[YearMonth] {
      def format(temporal: YearMonth): String = {
        formatter.format(temporal)
      }
    }

  /**
   * The default typeclass to write a `java.time.YearMonth`,
   */
  implicit val writesYearMonth: Writes[YearMonth] =
    temporalWrites[YearMonth, DateTimeFormatter](
      DateTimeFormatter.ofPattern("yyyy-MM")
    )
}
