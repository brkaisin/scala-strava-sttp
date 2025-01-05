package be.brkaisin.strava.models

case class DetailedClub(
    id: Long,
    name: String,
    profileMedium: String,
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
    url: String,
    membership: String,
    admin: Boolean,
    owner: Boolean,
    followingCount: Int
)
