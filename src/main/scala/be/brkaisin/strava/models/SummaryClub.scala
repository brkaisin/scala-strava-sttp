package be.brkaisin.strava.models

case class SummaryClub(
    id: Long,
    name: String,
    profileMedium: String,
    profile: String,
    coverPhoto: String,
    coverPhotoSmall: String,
    activityTypes: List[SportType],
    city: String,
    state: String,
    country: String,
    `private`: Boolean,
    memberCount: Int,
    featured: Boolean,
    verified: Boolean,
    url: String
)
