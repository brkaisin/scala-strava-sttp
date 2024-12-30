package be.brkaisin.strava.api

trait StravaApi[+R[_]]:
  def activity: ActivityApi[R]

  def athlete: AthleteApi[R]
