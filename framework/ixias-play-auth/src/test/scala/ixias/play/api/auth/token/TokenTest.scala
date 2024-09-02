package ixias.play.api.auth.token

import munit.FunSuite
import play.api.mvc.{Headers, RequestHeader, Result, Results, Cookies, Cookie}
import ixias.play.api.auth.token.{Token, TokenViaHttpHeader, TokenViaSession}
import play.api.libs.typedmap.TypedMap
import play.api.mvc.request.{RemoteConnection, RequestTarget}

import ixias.security.TokenSigner

class TokenTest extends FunSuite {

  private val tokenValue = "test-token"
  private val authenticityToken = Token.AuthenticityToken(tokenValue)
  private val result: Result = Results.Ok
  private val token = TokenViaSession("test")

  // Mock RequestHeader
  implicit val request: RequestHeader = new RequestHeader {
    override def method = ""
    override def version = ""
    override def headers = new Headers(Seq.empty)

    override def connection: RemoteConnection = new RemoteConnection {
      override def remoteAddress: java.net.InetAddress = java.net.InetAddress.getByName("127.0.0.1")
      override def secure: Boolean = false
      override def clientCertificateChain: Option[Seq[java.security.cert.X509Certificate]] = None
    }

    override def target: RequestTarget = new RequestTarget {
      override def uri: java.net.URI = java.net.URI.create("/")
      override def uriString: String = "/"
      override def path: String = "/"
      override def queryMap: Map[String, Seq[String]] = Map.empty
    }

    override def attrs: TypedMap = TypedMap.empty
    override def cookies: Cookies = Cookies(Seq(Cookie(token.cookieName, TokenSigner().sign(tokenValue))))
  }

  test("TokenViaHttpHeader should put a token in the headers") {
    val token = TokenViaHttpHeader("test")
    val resultWithToken = token.put(authenticityToken)(result)
    assert(resultWithToken.header.headers.contains(token.headerName))
  }

  test("TokenViaHttpHeader should discard a token from the headers") {
    val token = TokenViaHttpHeader("test")
    val resultWithoutToken = token.discard(result)
    assert(!resultWithoutToken.header.headers.contains(token.headerName))
  }

  test("TokenViaHttpHeader should extract a token from the headers") {
    val token = TokenViaHttpHeader("test")
    val requestWithToken = request.withHeaders(Headers(token.headerName -> TokenSigner().sign(tokenValue)))
    val extractedToken = token.extract(requestWithToken)
    assert(extractedToken.contains(authenticityToken))
  }

  test("TokenViaSession should put a token in the cookies") {
    val token = TokenViaSession("test")
    val resultWithToken = token.put(authenticityToken)(result)
    assert(resultWithToken.newCookies.exists(_.name == token.cookieName))
  }

  test("TokenViaSession should discard a token from the cookies") {
    val token = TokenViaSession("test")
    val resultWithoutToken = token.discard(result)
    val discardedCookies = resultWithoutToken.newCookies.filter(_.maxAge.contains(0))
    assert(discardedCookies.exists(_.name == token.cookieName))
  }

  test("TokenViaSession should extract a token from the cookies") {
    val extractedToken = token.extract(request)
    assert(extractedToken.contains(authenticityToken))
  }
}
