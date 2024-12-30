package be.brkaisin.strava.models

import java.time.Instant

case class SummaryAthlete(
    id: Long,
    firstname: String,
    lastname: String,
    profileMedium: String,
    profile: String,
    city: Option[String],
    state: Option[String],
    country: Option[String],
    sex: AthleteSex,
    summit: Boolean,
    createdAt: Instant,
    updatedAt: Instant,
    // fields not described in the Strava API documentation but present in the JSON response
    username: Option[String],
    bio: Option[String],
    badgeTypeId: Option[Int],
    weight: Option[Float],
    friend: Option[Boolean],
    follower: Option[Boolean]
)
