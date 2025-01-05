package be.brkaisin.strava.models

case class ClubActivity(
    athlete: MetaAthlete,
    name: String,
    distance: Float,
    movingTime: Int,
    elapsedTime: Int,
    totalElevation_gain: Float,
    sportType: SportType,
    workoutType: Option[Int] // todo: check optionality
)
