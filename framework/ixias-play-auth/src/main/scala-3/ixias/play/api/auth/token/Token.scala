/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.auth.token

import ixias.security.TokenSigner
import ixias.util.{ Configuration, Logging }
import play.api.mvc.{ RequestHeader, Result }

// The security token
//~~~~~~~~~~~~~~~~~~~~
trait Token {
  import Token._

  /** The configuration */
  protected val config: Configuration = Configuration()

  /** Put a specified security token to storage */
  def put(token: AuthenticityToken)(result: Result)(implicit request: RequestHeader): Result

  /** Discard a security token in storage */
  def discard(result: Result)(implicit request: RequestHeader): Result

  /** Extract a security token from storage */
  def extract(implicit request: RequestHeader): Option[AuthenticityToken]
}

// Companion object
//~~~~~~~~~~~~~~~~~~
object Token extends Logging {

  opaque type SignedToken       = String
  opaque type AuthenticityToken = String

  object SignedToken {
    def apply(value:  String):      SignedToken = value
    def unwrap(token: SignedToken): String      = token
  }

  object AuthenticityToken {
    def apply(value:  String):            AuthenticityToken = value
    def unwrap(token: AuthenticityToken): String            = token
  }

  /** The object that provides some cryptographic operations */
  protected lazy val signer: TokenSigner = TokenSigner()

  /** Verifies a given HMAC on a piece of data */
  final def verifyHMAC(signedToken: SignedToken): Option[AuthenticityToken] =
    signer.verify(SignedToken.unwrap(signedToken)) match {
      case scala.util.Success(v) => Some(AuthenticityToken(v))
      case scala.util.Failure(ex) => {
        logger.warn("Token's HMAC verification failed. %s", ex)
        None
      }
    }

  /** Signs the given String with HMAC-SHA1 using the secret token. */
  final def signWithHMAC(token: AuthenticityToken): SignedToken =
    SignedToken(signer.sign(AuthenticityToken.unwrap(token)))
}
