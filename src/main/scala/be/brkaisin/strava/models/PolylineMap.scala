package be.brkaisin.strava.models

sealed trait PolylineMap:
  val id: String
  val summaryPolyline: String

object PolylineMap:
  case class SummaryPolylineMap(
      id: String,
      summaryPolyline: String
  ) extends PolylineMap

  case class CompletePolylineMap(
      id: String,
      polyline: String,
      summaryPolyline: String
  ) extends PolylineMap
