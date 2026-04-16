package shade.memcached

import scala.concurrent.duration.FiniteDuration

final case class Configuration(
  addresses: String,
  keysPrefix: Option[String] = None,
  operationTimeout: FiniteDuration,
)
