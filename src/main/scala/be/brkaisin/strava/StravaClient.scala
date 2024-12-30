package be.brkaisin.strava

import be.brkaisin.strava.api.{ActivityApi, AthleteApi, StravaApi}
import be.brkaisin.strava.circe.Decoders.given
import be.brkaisin.strava.models.{
  Comment,
  DetailedAthlete,
  SummaryActivity,
  SummaryClub
}
import be.brkaisin.strava.utils.SttpUtils
import be.brkaisin.strava.utils.SttpUtils.monad
import io.circe.Decoder
import sttp.client4.*
import sttp.client4.httpclient.{HttpClientFutureBackend, HttpClientSyncBackend}
import sttp.model.Header
import sttp.shared.Identity

import scala.concurrent.{ExecutionContext, Future}

class StravaClient[F[_]: Backend](accessToken: String):
  type Safe[T]   = SafeF[F, T]
  type Unsafe[T] = UnsafeF[F, T]

  private val baseUrl: String = "https://www.strava.com/api/v3"

  private val authHeader: Header =
    Header("Authorization", s"Bearer $accessToken")

  private def get[T: Decoder](url: String): Safe[T] = SttpUtils.get(
    uri"${s"$baseUrl$url"}",
    authHeader
  )

  lazy val api: StravaApi[Safe] = new StravaApi[Safe]:
    lazy val activity: ActivityApi[Safe] = new ActivityApi[Safe]:
      def getLoggedInAthleteActivities(
          before: Option[Int],
          after: Option[Int],
          page: Int,
          perPage: Int
      ): Safe[List[SummaryActivity]] =
        get[List[SummaryActivity]]("/athlete/activities")

      def getCommentsByActivityId(
          id: Long,
          pageSize: Int,
          afterCursor: Option[String]
      ): Safe[List[Comment]] = get[List[Comment]](
        s"/activities/$id/comments?page_size=$pageSize&after_cursor=$afterCursor"
      )

    lazy val athlete: AthleteApi[Safe] = new AthleteApi[Safe]:
      def getLoggedInAthlete: Safe[DetailedAthlete] =
        get[DetailedAthlete]("/athlete")

      def getLoggedInAthleteClubs(
          page: Int,
          perPage: Int
      ): Safe[List[SummaryClub]] = get[List[SummaryClub]](
        s"/athlete/clubs?page=$page&per_page=$perPage"
      )

  lazy val unsafeApi: StravaApi[Unsafe] = new StravaApi[Unsafe]:
    lazy val activity: ActivityApi[Unsafe] = new ActivityApi[Unsafe]:
      def getLoggedInAthleteActivities(
          before: Option[Int],
          after: Option[Int],
          page: Int,
          perPage: Int
      ): Unsafe[List[SummaryActivity]] = unsafe(
        api.activity
          .getLoggedInAthleteActivities(before, after, page, perPage)
      )

      def getCommentsByActivityId(
          id: Long,
          pageSize: Int,
          afterCursor: Option[String]
      ): Unsafe[List[Comment]] = unsafe(
        api.activity.getCommentsByActivityId(id, pageSize, afterCursor)
      )

    lazy val athlete: AthleteApi[Unsafe] = new AthleteApi[Unsafe]:
      def getLoggedInAthlete: Unsafe[DetailedAthlete] = unsafe(
        api.athlete.getLoggedInAthlete
      )

      def getLoggedInAthleteClubs(
          page: Int,
          perPage: Int
      ): Unsafe[List[SummaryClub]] = unsafe(
        api.athlete.getLoggedInAthleteClubs(page, perPage)
      )

object StravaClient:

  def effect[F[_]: Backend](accessToken: String): StravaClient[F] =
    new StravaClient[F](accessToken)

  def sync(accessToken: String): StravaClient[Identity] =
    effect[Identity](accessToken)(using HttpClientSyncBackend())

  def future(accessToken: String)(implicit
      ec: ExecutionContext = ExecutionContext.global
  ): StravaClient[Future] =
    effect[Future](accessToken)(using HttpClientFutureBackend())
