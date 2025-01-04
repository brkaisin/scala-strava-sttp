package be.brkaisin.strava.circe

import be.brkaisin.strava.auth.TokenResponse
import be.brkaisin.strava.circe.Derivation.derivedCamelDecoder
import be.brkaisin.strava.models.PolylineMap.SummaryPolylineMap
import be.brkaisin.strava.models.*
import io.circe.{Decoder, HCursor}

object Decoders:
  given tokenResponseDecoder: Decoder[TokenResponse] = derivedCamelDecoder
  given errorDecoder: Decoder[Error]                 = derivedCamelDecoder
  given faultDecoder: Decoder[Fault]                 = derivedCamelDecoder

  given Decoder[SummaryActivity] = derivedCamelDecoder

  given Decoder[MetaAthlete] = derivedCamelDecoder

  given Decoder[SummaryPolylineMap] = derivedCamelDecoder

  given Decoder[SportType] = Decoder.decodeString.emap { str =>
    SportType.values
      .find(_.toString == str)
      .toRight(s"Invalid SportType: $str")
  }

  given Decoder[LatLng] = (c: HCursor) =>
    for
      latitude  <- c.downN(0).as[Float]
      longitude <- c.downN(1).as[Float]
    yield LatLng(latitude = latitude, longitude = longitude)

  given Decoder[AthleteSex] = Decoder.decodeString.emap { str =>
    AthleteSex.values
      .find(_.toString == str)
      .toRight(s"Invalid AthleteSex: $str")
  }

  given Decoder[MeasureUnit] = Decoder.decodeString.emap { str =>
    MeasureUnit.values
      .find(_.toString.toLowerCase == str)
      .toRight(s"Invalid MeasureUnit: $str")
  }

  given Decoder[SummaryClub] = derivedCamelDecoder

  given Decoder[SummaryGear] = derivedCamelDecoder

  given Decoder[DetailedAthlete] = derivedCamelDecoder

  given Decoder[SummaryAthlete] = derivedCamelDecoder

  given Decoder[Comment] = derivedCamelDecoder

  given Decoder[ZoneRange] = derivedCamelDecoder

  given Decoder[HeartRateZoneRanges] = derivedCamelDecoder

  given Decoder[PowerZoneRanges] = derivedCamelDecoder

  given Decoder[Zones] = derivedCamelDecoder
