package be.brkaisin.strava

/** A sealed trait representing the various permission scopes available in the
  * Strava API.
  *
  * Each permission corresponds to a specific access level or functionality that
  * can be granted to an application interacting with the Strava API.
  * Permissions can be combined to create a scope string used during
  * authorization.
  *
  * Example:
  * {{{
  * val permissions = Set(StravaPermission.ActivityWrite, StravaPermission.Read)
  * val scopeString = StravaPermission.toScopeString(permissions)
  * println(scopeString) // Output: "activity:write,read"
  * }}}
  */
sealed trait StravaPermission:
  def value: String

object StravaPermission:
  case object Read extends StravaPermission:
    val value: String = "read"

  case object ReadAll extends StravaPermission:
    val value: String = "read_all"

  case object ProfileReadAll extends StravaPermission:
    val value: String = "profile:read_all"

  case object ProfileWrite extends StravaPermission:
    val value: String = "profile:write"

  case object ActivityRead extends StravaPermission:
    val value: String = "activity:read"

  case object ActivityReadAll extends StravaPermission:
    val value: String = "activity:read_all"

  case object ActivityWrite extends StravaPermission:
    val value: String = "activity:write"

  /** Converts a set of permissions into a comma-separated scope string.
    *
    * @param permissions
    *   A set of permissions to include in the scope.
    * @return
    *   A comma-separated string representing the combined permissions.
    */
  def toScopeString(permissions: Set[StravaPermission]): String =
    permissions.map(_.value).mkString(",")
