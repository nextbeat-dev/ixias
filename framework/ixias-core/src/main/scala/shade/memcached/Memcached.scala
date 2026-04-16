package shade.memcached

import scala.concurrent.{ Future, blocking }
import scala.concurrent.duration.Duration

import net.spy.memcached.{ AddrUtil, ConnectionFactoryBuilder, MemcachedClient }
import net.spy.memcached.transcoders.SerializingTranscoder

import ixias.persistence.dbio.Execution

final class Memcached private (client: MemcachedClient, config: Configuration) {
  private implicit val ec = Execution.Implicits.trampoline
  private val transcoder  = new SerializingTranscoder()

  private def prefixed(key: String): String =
    config.keysPrefix.fold(key)(_ + key)

  private def expiryInSeconds(expiry: Duration): Int =
    if (expiry.isFinite) math.max(0, expiry.toSeconds.toInt) else 0

  def get[A](key: String): Future[Option[A]] =
    Future {
      blocking {
        Option(client.get(prefixed(key), transcoder)).map(_.asInstanceOf[A])
      }
    }

  def add[A](key: String, value: A, expiry: Duration): Future[Unit] =
    Future {
      blocking {
        client
          .add(prefixed(key), expiryInSeconds(expiry), value.asInstanceOf[AnyRef], transcoder)
          .get(config.operationTimeout.length, config.operationTimeout.unit)
        ()
      }
    }

  def set[A](key: String, value: A, expiry: Duration): Future[Unit] =
    Future {
      blocking {
        client
          .set(prefixed(key), expiryInSeconds(expiry), value.asInstanceOf[AnyRef], transcoder)
          .get(config.operationTimeout.length, config.operationTimeout.unit)
        ()
      }
    }

  def delete(key: String): Future[Unit] =
    Future {
      blocking {
        client
          .delete(prefixed(key))
          .get(config.operationTimeout.length, config.operationTimeout.unit)
        ()
      }
    }
}

object Memcached {
  def apply(config: Configuration): Memcached = {
    val factory = new ConnectionFactoryBuilder()
      .setOpTimeout(config.operationTimeout.toMillis)
      .build()
    val client = new MemcachedClient(factory, AddrUtil.getAddresses(config.addresses))
    new Memcached(client, config)
  }
}
