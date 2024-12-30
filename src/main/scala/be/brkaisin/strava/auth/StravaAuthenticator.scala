package be.brkaisin.strava.auth

import be.brkaisin.strava.circe.Decoders.tokenResponseDecoder
import be.brkaisin.strava.utils.SttpUtils
import be.brkaisin.strava.utils.SttpUtils.monad
import be.brkaisin.strava.{unsafe as unsafeF, SafeF, UnsafeF}
import sttp.client4.*
import sttp.client4.httpclient.{HttpClientFutureBackend, HttpClientSyncBackend}
import sttp.shared.Identity

import scala.concurrent.{ExecutionContext, Future}

class StravaAuthenticator[F[_]: Backend](
    clientId: String,
    clientSecret: String
) extends StravaAuthenticator.Api[StravaAuthenticator[F]#Safe]:
  self =>
  type Safe[T]   = SafeF[F, T]
  type Unsafe[T] = UnsafeF[F, T]

  private val tokenUrl: String = "https://www.strava.com/oauth/token"

  def exchangeCodeForToken(code: String): Safe[TokenResponse] =
    SttpUtils.post(
      uri"$tokenUrl?client_id=$clientId&client_secret=$clientSecret&code=$code&grant_type=authorization_code"
    )

  // todo: maybe no athlete in response
  def refreshToken(refreshToken: String): Safe[TokenResponse] =
    SttpUtils.post(
      uri"$tokenUrl?client_id=$clientId&client_secret=$clientSecret&refresh_token=$refreshToken&grant_type=refresh_token"
    )

  def deauthorize(accessToken: String): Safe[Unit] =
    SttpUtils.post(
      uri"https://www.strava.com/api/v3/oauth/deauthorize?access_token=$accessToken"
    )

  lazy val unsafe: StravaAuthenticator.Api[Unsafe] =
    new StravaAuthenticator.Api[Unsafe]:
      def exchangeCodeForToken(
          code: String
      ): Unsafe[TokenResponse] = unsafeF(self.exchangeCodeForToken(code))

      def refreshToken(
          refreshToken: String
      ): Unsafe[TokenResponse] = unsafeF(self.refreshToken(refreshToken))

      def deauthorize(accessToken: String): Unsafe[Unit] =
        unsafeF(self.deauthorize(accessToken))

object StravaAuthenticator:

  private[auth] trait Api[+R[_]]:
    def exchangeCodeForToken(code: String): R[TokenResponse]
    def refreshToken(refreshToken: String): R[TokenResponse]
    def deauthorize(accessToken: String): R[Unit]

  def effect[F[_]: Backend](
      clientId: String,
      clientSecret: String
  ): StravaAuthenticator[F] = new StravaAuthenticator[F](clientId, clientSecret)

  def sync(
      clientId: String,
      clientSecret: String
  ): StravaAuthenticator[Identity] =
    effect[Identity](clientId, clientSecret)(using HttpClientSyncBackend())

  def future(clientId: String, clientSecret: String)(implicit
      ec: ExecutionContext = ExecutionContext.global
  ): StravaAuthenticator[Future] =
    effect[Future](clientId, clientSecret)(using HttpClientFutureBackend())
