package be.brkaisin.strava.models

case class Error(
    code: String,
    field: String,
    resource: String
)
