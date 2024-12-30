package be.brkaisin.strava.api

import be.brkaisin.strava.models.{Comment, SummaryActivity}

trait ActivityApi[+R[_]]:
  def getLoggedInAthleteActivities(
      before: Option[Int] = None,
      after: Option[Int] = None,
      page: Int = 1,
      perPage: Int = 30
  ): R[List[SummaryActivity]]

  def getCommentsByActivityId(
      id: Long,
      pageSize: Int = 30,
      afterCursor: Option[String] = None
  ): R[List[Comment]]
