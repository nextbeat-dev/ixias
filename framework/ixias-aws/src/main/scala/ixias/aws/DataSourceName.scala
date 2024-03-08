package ixias.aws

import scala.util.matching.Regex

/**
 * The DSN(Data-Source-Name) structure.
 */
sealed class DataSourceName(
  val path:     String,
  val resource: String,
  val name:     Option[String]
) {
  override def toString: String = s"$path/$resource${name.map(v => s"/$v").getOrElse("")}"
}

object DataSourceName {
  /** The syntax format for DSN */
  private val SYNTAX_DATA_SOURCE_NAME1: Regex = """^([.\w]+)://(\w+?)$""".r
  private val SYNTAX_DATA_SOURCE_NAME2: Regex = """^([.\w]+)://(\w+?)/(\w+)$$""".r

  /** Build a `DataSourceName` object. */
  def apply(dsn: String): DataSourceName = dsn match {
    case SYNTAX_DATA_SOURCE_NAME1(p1, p2)     => new DataSourceName(p1, p2, None)
    case SYNTAX_DATA_SOURCE_NAME2(p1, p2, p3) => new DataSourceName(p1, p2, Some(p3))
    case _ => throw new IllegalArgumentException(s"""Dose not match the DSN format. ($dsn)""")
  }
}
