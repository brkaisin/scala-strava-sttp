package be.brkaisin.strava.api

import be.brkaisin.strava.models.*

trait ClubApi[+R[_]]:
  def getClubActivitiesById(
      id: Long,
      page: Int = 1,
      perPage: Int = 30
  ): R[List[ClubActivity]]

  def getClubAdminsById(
      id: Long,
      page: Int = 1,
      perPage: Int = 30
  ): R[List[SummaryAthlete]]

  def getClubById(id: Long): R[DetailedClub]

  def getClubMembersById(
      id: Long,
      page: Int = 1,
      perPage: Int = 30
  ): R[List[ClubAthlete]]

  def getLoggedInAthleteClubs(
      page: Int = 1,
      perPage: Int = 30
  ): R[List[SummaryClub]]
