package be.brkaisin.strava.circe

import io.circe.{Decoder, DecodingFailure, HCursor}

import scala.compiletime.{constValue, erasedValue, summonFrom, summonInline}
import scala.deriving.Mirror

/* Semi-automatic derivation of decoders for case classes with camelCase keys from JSON with snake_case keys. */
object Derivation:
  // same as io.circe.Derivation.summonLabelsRec, but copied here to avoid the `circe-generic` dependency
  private inline def summonLabelsRec[T <: Tuple]: List[String] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (t *: ts) =>
        constValue[t].asInstanceOf[String] :: summonLabelsRec[ts]

  // same as `io.circe.Derivation.summonDecodersRec`, but without `Decoder.derived[A]` with a Mirror when no decoder is
  // "summonable" to force defining a decoder for each nested type (semi-automatic instead of automatic derivation)
  private inline def summonDecodersRec[T <: Tuple]: List[Decoder[?]] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (t *: ts)  => summonInline[Decoder[t]] :: summonDecodersRec[ts]

  private def camelToSnake(s: String): String =
    s.flatMap {
      case c if c.isUpper => "_" + c.toLower
      case c              => c.toString
    }.stripPrefix("_")

  /** Derive a decoder for a case class with camelCase keys from a JSON with
    * associated snake_case keys. It is inspired by circe's `deriveDecoder,` but
    * it converts the keys from snake_case to camelCase. Note that the
    * conversion is done at runtime, so it may have a performance impact.
    * Relying on a macro-based solution would be more efficient.
    *
    * NB: `circe-generic-extras` provides a similar feature, but it is not
    * ported to Scala 3.
    */
  inline def derivedCamelDecoder[T](using m: Mirror.ProductOf[T]): Decoder[T] =
    val elemLabels: List[String]   = summonLabelsRec[m.MirroredElemLabels]
    val decoders: List[Decoder[?]] = summonDecodersRec[m.MirroredElemTypes]

    (c: HCursor) =>
      for
        values <- elemLabels
          .zip(decoders)
          .foldLeft(Right(Vector.empty): Either[DecodingFailure, Vector[?]]) {
            case (acc, (label, decoder)) =>
              acc.flatMap(vec =>
                decoder
                  .tryDecode(c.downField(camelToSnake(label)))
                  .map(value => vec :+ value)
              )
          }
      yield m.fromProduct(Tuple.fromArray(values.toArray))
