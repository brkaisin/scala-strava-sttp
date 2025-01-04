package be.brkaisin.strava.models

case class HeartRateZoneRanges(
    customZones: Boolean,
    zones: List[ZoneRange]
)
