package be.brkaisin.strava.auth

import be.brkaisin.strava.StravaPermission

case class AuthorizationUrl(
    clientId: String,
    redirectUri: String,
    permissions: List[StravaPermission],
    approvalPrompt: "force" | "auto" = "auto"
):
  def build: String =
    val scope: String = StravaPermission.toScopeString(permissions.toSet)
    s"https://www.strava.com/oauth/authorize?client_id=$clientId&response_type=code&redirect_uri=$redirectUri&approval_prompt=$approvalPrompt&scope=$scope"
