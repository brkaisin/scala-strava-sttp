package be.brkaisin.strava.models

case class ClubAthlete(
    firstname: String,
    lastname: String,
    member: String, // todo: check if it's not "membership" in practice
    admin: Boolean,
    owner: Boolean
)
