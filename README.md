# scala-strava-sttp

scala-strava-sttp is a Scala 3 library for accessing the [Strava v3 API](https://developers.strava.com/docs/reference/)
using the powerful [sttp client](https://github.com/softwaremill/sttp) library.

This library simplifies the process of interacting with the Strava API, providing a clean and idiomatic Scala interface 
for developers.

## Features
- Strava v3 API support: access the full range of Strava API endpoints for activities, athletes, segments, and more.
- Powered by sttp: built on top of sttp, offering generic and flexible HTTP client capabilities.
- [Circe](https://github.com/circe/circe): responses are automatically parsed into case classes using Circe.
- Asynchronous and Functional: designed for modern Scala applications with support for Cats Effect, ZIO, or any other 
  backend supported by sttp.
- Easy Configuration: simplify the integration with minimal setup.

## Installation

**TODO WHEN PUBLISHED**

## Setup

1. Obtain your Strava API credentials (client ID and client secret) by registering an application at the 
   [Strava Developers Portal](https://developers.strava.com/).
2. Retrieve an access token using the OAuth2 authorization code flow. See the 
   [Strava API documentation](https://developers.strava.com/docs/getting-started/) for more information, and/or read the
   **OAuth2 Authorization Code Flow** section later in this README.
3. Import the library and initialize your client.


## Usage

You can configure the library to use any sttp backend (e.g., AsyncHttpClient, ZIO, Cats Effect). **In this section, we
assume you dispose of an access token.**

### Synchronous Usage

Here is an example of how to use the library synchronously with the `HttpClientSyncBackend` from sttp:

```scala
import be.brkaisin.strava.*
import be.brkaisin.strava.api.ActivityApi
import be.brkaisin.strava.models.SummaryActivity
import sttp.shared.Identity

val syncStravaClient: StravaClient[Identity] = StravaClient.sync("ACCESS_TOKEN")

val activityApi: ActivityApi[SafeIdentity] = syncStravaClient.api.activity

val activities: SafeIdentity[List[SummaryActivity]] =
  activityApi.getLoggedInAthleteActivities()
```

In this example, we use the `Identity` monad from sttp to run the requests synchronously. Type `SafeIdentity[T]` is an
alias for `Either[sttp.client4.ResponseException[be.brkaisin.strava.models.Fault, io.circe.Error], T]`, meaning that the
response is either a successful value `T` or a sttp exception, which can be a Strava API error or a Circe decoding
error.

### If you like playing with fire

Depending on your needs and situation, you may not want to be burdened by this type of Either and opt for a more
"Java-like" approach (no offense). In this case, it's very simple: replace the last two `val`'s above with:

```scala
val unsafeActivityApi: ActivityApi[UnsafeIdentity] =
  syncStravaClient.unsafeApi.activity

val unsafeActivities: UnsafeIdentity[List[SummaryActivity]] =
  unsafeActivityApi.getLoggedInAthleteActivities()
```  

As you may have guessed, `UnsafeIdentity[T]` is an alias for `T`, meaning that the response is always a successful value
`T`, and you will have to handle the exceptions yourself, or let your program crash.

> [!NOTE]  
> You can also achieve the same result by using the `unsafe` extension method on "Safe" types, like this:
> ```scala
> val unsafeActivities: List[SummaryActivity] = activityApi.getLoggedInAthleteActivities().unsafe
> ```

### Asynchronous Usage

Here is an example of how to use the library asynchronously with the `HttpClientFutureBackend` from sttp:

```scala
import be.brkaisin.strava.*
import be.brkaisin.strava.api.ActivityApi
import be.brkaisin.strava.models.{Fault, SummaryActivity}
import io.circe
import sttp.client4.ResponseException

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

given ExecutionContext = ExecutionContext.global

val futureStravaClient: StravaClient[Future] =
  StravaClient.future("ACCESS_TOKEN")

val activitiesResponse: SafeFuture[List[SummaryActivity]] =
  futureStravaClient.api.activity.getLoggedInAthleteActivities()

activitiesResponse.onComplete {
  case Success(safeResponse) =>
    safeResponse match
  case Right(activities: List[SummaryActivity]) =>
    activities.foreach(println)
  case Left(error: ResponseException[Fault, circe.Error]) => println(error)
  case Failure(exception) => println(exception)
}
```

Again, you can play with fire if you want:

```scala
val unsafeActivitiesResponse: UnsafeFuture[List[SummaryActivity]] =
  futureStravaClient.unsafeApi.activity.getLoggedInAthleteActivities()

unsafeActivitiesResponse.onComplete {
  case Success(activities: List[SummaryActivity]) => activities.foreach(println)
  case Failure(exception)                         => println(exception)
}
```

## OAuth2 Authorization Code Flow

The Strava API uses OAuth2 for authentication and authorization. To access the API on behalf of a user, you need to
obtain an access token using the OAuth2 authorization code flow. Here is a brief overview of the steps involved:

1. **Register your application:** Go to the [Strava Developers Portal](https://developers.strava.com/) and create a new
   application to obtain your client ID and client secret.
2. **Redirect the user to the authorization URL:** Construct the authorization URL with your client ID and the desired
   scopes. Redirect the user to this URL to grant access to your application. Here is an example of how to construct the authorization URL:
    ```scala
    import be.brkaisin.strava.StravaPermission
    import be.brkaisin.strava.auth.AuthorizationUrl
    
    val authorizationUrl: String = AuthorizationUrl(
      clientId = "123456",
      redirectUri = "http://localhost:8080/catch-code",
      permissions = List(StravaPermission.Read, StravaPermission.ActivityRead),
      approvalPrompt = "force"
    ).build
    ```
    
    This will generate the following URL:
    
    ```
    https://www.strava.com/oauth/authorize?client_id=123456&response_type=code&redirect_uri=http://localhost:8080/catch-code&approval_prompt=force&scope=read,activity:read
    ```
3. **Receive the authorization code:** After the user grants access, they will be redirected back to your application
   with an authorization code.
4. **Exchange the authorization code for an access token:** Use the authorization code to request an access token from
   the Strava API.

Here is a minimal example of how to obtain an access token using [http4s](https://http4s.org/) and 
[cats-effect](https://typelevel.org/cats-effect/). First, add the following dependencies to your `build.sbt`:
    
```scala
libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % "1.0.0-M44",
  "org.http4s" %% "http4s-ember-server" % "1.0.0-M44",
  "org.typelevel" %% "log4cats-slf4j" % "2.7.0",
  "org.slf4j" % "slf4j-simple" % "2.0.16"
)
```

Then, create a simple HTTP server to handle the OAuth2 authorization code flow:

```scala
import cats.data.Kleisli
import cats.effect.*
import com.comcast.ip4s.{Host, Port}
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.{HttpRoutes, Request, Response}
import org.typelevel.log4cats.*
import org.typelevel.log4cats.slf4j.Slf4jFactory

object StravaAuthServer extends IOApp:

  given LoggerFactory[IO] = Slf4jFactory.create[IO]
  private val logger: SelfAwareStructuredLogger[IO] =
    LoggerFactory[IO].getLogger

  def run(args: List[String]): IO[ExitCode] =
    val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
      case GET -> Root / "catch-code" :? CodeQueryParamMatcher(code) =>
        logger.info(s"Authorization code: $code") *> Ok(
          "Authorization successful! You can close this page."
        )
    }

    val app: Kleisli[IO, Request[IO], Response[IO]] = routes.orNotFound

    EmberServerBuilder
      .default[IO]
      .withHost(Host.fromString("localhost").get)
      .withPort(Port.fromInt(8080).get)
      .withHttpApp(app)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)

  private object CodeQueryParamMatcher
      extends QueryParamDecoderMatcher[String]("code")
```

This server listens on `http://localhost:8080/catch-code` and logs the authorization code when the user is redirected
back to the server.

## API Documentation [WIP]

The library provides easy-to-use methods for common Strava API operations, such as:
- **Activity endpoints:**
    - createActivity: Create a new activity.
    - getActivityById(activityId): Fetch details of a specific activity.
    - getCommentsByActivityId(activityId): Fetch comments for a specific activity.
    - ...
- **Athlete endpoints:**
    - getLoggedInAthlete: Fetches the authenticated athlete’s profile.
    - getStats: Retrieves stats for the authenticated athlete.
    - updateAthlete: Updates the authenticated athlete’s profile.
    - ...
- **Segment endpoints:**
    - exploreSegments: Discover popular segments in a specific area.
    - getLoggedInAthleteStarredSegments: Fetches the authenticated athlete’s starred segments.
    - ...
- **Club endpoints:**
    - getClubById(clubId): Fetch details of a specific club.
    - getLoggedInAthleteClubs: Fetches clubs that the authenticated athlete is a member of.
    - ...
- **Route endpoints:**
    - getRouteById(routeId): Fetch details of a specific route.
    - getLoggedInAthleteRoutes: Fetches routes created by the authenticated athlete.
    - getRouteAsGPX(routeId): Fetches a route as a GPX file.
    - ...

For a full list of supported methods, check the source code and examples.

## To Do

- [ ] Implement more API endpoints
- [ ] Add more examples and documentation
- [ ] Add support for more sttp backends (new subprojects)
- [ ] Write tests
- [ ] Perhaps give the possibility to use other JSON libraries than Circe, to avoid forcing the dependency
- [ ] Publish the library to Maven Central

## Contributing

Contributions are welcome! Please follow these steps:
1.	Fork the repository
2.	Create a feature branch
3.	Submit a pull request with detailed explanations of your changes

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE.txt) file for details.