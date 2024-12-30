package be.brkaisin.strava

import be.brkaisin.strava.models.Fault
import io.circe
import sttp.client4.ResponseException
import sttp.monad.MonadError
import sttp.monad.syntax.MonadErrorOps
import sttp.shared.Identity

import scala.concurrent.Future

private type SafeResponse[+T] =
  Either[ResponseException[Fault, circe.Error], T]
private type UnsafeResponse[+T] = T

type SafeF[+F[_], T]   = F[SafeResponse[T]]
type UnsafeF[+F[_], T] = F[UnsafeResponse[T]]

extension [T](safeResponse: SafeResponse[T])
  def unsafe: UnsafeResponse[T] = safeResponse.toTry.get

def unsafe[F[_]: MonadError, T](
    response: F[SafeResponse[T]]
): F[UnsafeResponse[T]] = response.map(_.unsafe)

// Useful type aliases

type SafeIdentity[T]   = SafeF[Identity, T]
type UnsafeIdentity[T] = UnsafeF[Identity, T]

type SafeFuture[T]   = SafeF[Future, T]
type UnsafeFuture[T] = UnsafeF[Future, T]
