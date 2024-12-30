package be.brkaisin.strava.models

case class Fault(
    errors: List[Error],
    message: String
)
