package be.brkaisin.strava.models

import java.time.LocalDateTime

case class DetailedAthlete(
    id: Long,
    firstName: String,
    lastName: String,
    profileMedium: String,
    profile: String,
    city: String,
    state: String,
    country: String,
    sex: AthleteSex,
    summit: Boolean,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime,
    followerCount: Int,
    friendCount: Int,
    measurementPreference: MeasureUnit,
    ftp: Option[Int],
    weight: Float,
    clubs: List[SummaryClub],
    bikes: List[SummaryGear],
    shoes: List[SummaryGear]
)
