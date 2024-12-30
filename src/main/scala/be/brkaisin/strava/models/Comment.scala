package be.brkaisin.strava.models

import java.time.Instant

case class Comment(
    id: Long,
    activityId: Long,
    text: String,
    athlete: SummaryAthlete,
    createdAt: Instant
)
