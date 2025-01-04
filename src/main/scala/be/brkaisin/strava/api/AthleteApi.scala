package be.brkaisin.strava.api

import be.brkaisin.strava.models.{DetailedAthlete, SummaryClub, Zones}

trait AthleteApi[+R[_]]:
  def getLoggedInAthlete: R[DetailedAthlete]

  def getLoggedInAthleteZones: R[Zones]

  def getLoggedInAthleteClubs(
      page: Int = 1,
      perPage: Int = 30
  ): R[List[SummaryClub]]
