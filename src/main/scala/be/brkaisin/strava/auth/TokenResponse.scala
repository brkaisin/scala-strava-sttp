package be.brkaisin.strava.auth

import be.brkaisin.strava.models.SummaryAthlete

case class TokenResponse(
    accessToken: String,
    refreshToken: String,
    expiresAt: Long,
    expiresIn: Long,
    athlete: SummaryAthlete
)
