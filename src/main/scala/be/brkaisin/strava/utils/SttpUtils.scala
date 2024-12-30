package be.brkaisin.strava.utils

import be.brkaisin.strava.SafeF
import be.brkaisin.strava.circe.Decoders.faultDecoder
import be.brkaisin.strava.models.Fault
import io.circe.Decoder
import sttp.client4.circe.asJsonEither
import sttp.client4.{basicRequest, Backend}
import sttp.model.{Header, Uri}
import sttp.monad.MonadError
import sttp.monad.syntax.MonadErrorOps

object SttpUtils:
  given monad[F[_]](using backend: Backend[F]): MonadError[F] = backend.monad

  def get[F[_], T: Decoder](uri: Uri, headers: Header*)(using
      backend: Backend[F]
  ): SafeF[F, T] = basicRequest
    .get(uri)
    .headers(headers*)
    .response(asJsonEither[Fault, T])
    .send(backend)
    .map(_.body)

  def post[F[_], T: Decoder](uri: Uri, headers: Header*)(using
      backend: Backend[F]
  ): SafeF[F, T] = basicRequest
    .post(uri)
    .headers(headers*)
    .response(asJsonEither[Fault, T])
    .send(backend)
    .map(_.body)
