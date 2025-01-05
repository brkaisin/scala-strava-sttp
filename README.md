# scala-strava-sttp

[scala-strava-sttp](https://github.com/brkaisin/scala-strava-sttp) is a Scala 3 library for accessing the 
[Strava v3 API](https://developers.strava.com/docs/reference/) using the powerful
[sttp client](https://github.com/softwaremill/sttp) library.

This library simplifies the process of interacting with the Strava API, providing a clean and idiomatic Scala interface 
for developers.

## Features [WIP]
- Strava v3 API support: access the full range of Strava API endpoints for activities, athletes, segments, and more.
- Powered by sttp: built on top of sttp, offering generic and flexible HTTP client capabilities.
- [Circe](https://github.com/circe/circe): responses are automatically parsed into case classes using Circe.
- Asynchronous and Functional: designed for modern Scala applications with support for Cats Effect, ZIO, or any other 
  backend supported by sttp.

## Philosophy

As a programmer, you are often faced to use an external library to interact with an API (or something else). Most of the 
time, these libraries are very opinionated and force you to use a specific effect type (ZIO, Cats Effect, Monix, etc.), 
and/or they are not flexible enough to adapt to your needs. You end up having to write a lot of (sometimes boilerplate)
code to transform the response into something you can work with.

This library aims to provide a good balance between flexibility and ease of use, by allowing you to choose the effect 
type you want to use, and by providing a simple and idiomatic Scala API. This is allowed by the inherent high level of
abstraction provided by the sttp library.

The foundations of this library are located in [globals.scala](src/main/scala/be/brkaisin/strava/globals.scala), where 
you can find the following types:

```scala
import be.brkaisin.strava.models.Fault
import io.circe
import sttp.client4.ResponseException

private type SafeResponse[+T] = Either[ResponseException[Fault, circe.Error], T]
private type UnsafeResponse[+T] = T

type SafeF[+F[_], T]   = F[SafeResponse[T]]
type UnsafeF[+F[_], T] = F[UnsafeResponse[T]]
```

These types are used throughout the library to represent the response of every request made to the Strava API. The
`SafeResponse` type speaks for itself: it means that the response is either a successful value `T` or a sttp exception, 
which can be a Strava API error or a Circe decoding error. The `UnsafeResponse` type is an alias for `T`, meaning that 
the response is always a successful value `T`, and you will have to handle the exceptions yourself, or let your program 
crash.

Last but not least, you can notice the abstraction of the effect type `F[_]` in the `SafeF` and `UnsafeF` types. This
allows you to use any effect type you want, as long as it is supported by sttp (or you can write your own backend).

## Installation

**TODO WHEN PUBLISHED**

## Usage overview

1. Obtain your Strava API credentials (client ID and client secret) by registering an application at the 
   [Strava Developers Portal](https://developers.strava.com/).
2. Retrieve an access token using the OAuth2 authorization code flow. See the 
   [Strava API documentation](https://developers.strava.com/docs/getting-started/) for more information, and/or read the
   [OAuth2 Authorization Code Flow](#oauth2-authorization-code-flow) section below.
3. Import the library and initialize your client. See the [Usage](#usage) section for more information.

## OAuth2 Authorization Code Flow

The Strava API uses OAuth2 for authentication and authorization. To access the API on behalf of a user, you need to
obtain an access token using the OAuth2 authorization code flow. Here is a brief overview of the steps involved:

1. **Register your application:** Go to the [Strava Developers Portal](https://developers.strava.com/) and create a new
   application to obtain your client ID and client secret.
2. **Redirect the user to the authorization URL:** Construct the authorization URL with your client ID and the desired
   scopes. Redirect the user to this URL to grant access to your application. Here is an example of how to construct the 
   authorization URL:
    ```scala
    import be.brkaisin.strava.StravaPermission
    import be.brkaisin.strava.auth.AuthorizationUrl
    
    val authorizationUrl: String = AuthorizationUrl(
      clientId = "CLIENT_ID", // Your Strava client ID
      redirectUri = "http://localhost:8080/catch-code", // Your redirect URI, configured in your Strava application
      permissions = List(StravaPermission.Read, StravaPermission.ActivityRead),
      approvalPrompt = "force"
    ).build
    ```

   This will generate the following URL:

    ```
    https://www.strava.com/oauth/authorize?client_id=CLIENT_ID&response_type=code&redirect_uri=http://localhost:8080/catch-code&approval_prompt=force&scope=read,activity:read
    ```
3. **Receive the authorization code:** After the user grants access, they will be redirected back to your application
   with an authorization code. An example of code catching is shown in the
   [Bonus](#bonus-minimal-http-server-catching-the-authorization-code) section later in this README.
4. **Exchange the authorization code for an access token:** Use the authorization code to request an access token from
   the Strava API. This is done by making a POST request to the token endpoint with the authorization code, client ID,
   and client secret. Here is how to do it with the library, synchronously and safely:
    ```scala
    import be.brkaisin.strava.*
    import be.brkaisin.strava.auth.{StravaAuthenticator, TokenResponse}
    import be.brkaisin.strava.models.Fault
    import io.circe
    import sttp.client4.ResponseException
    import sttp.shared.Identity
    
    val clientId     = "CLIENT_ID"
    val clientSecret = "CLIENT_SECRET"
    val code         = "CODE"
    
    val authenticator: StravaAuthenticator[Identity] =
    StravaAuthenticator.sync(clientId, clientSecret)
    
    // Either[ResponseException[Fault, circe.Error], TokenResponse]
    val tokenResponse: SafeIdentity[TokenResponse] = authenticator.exchangeCodeForToken(code)
    
    val accessToken: Either[ResponseException[Fault, circe.Error], String] = tokenResponse.map(_.accessToken)
    ```

    The `TokenResponse` case class contains the access token, refresh token, expiration time, and athlete information. 
    The library also allows to refresh the access token or to deauthorize it.

    Finally, you can also use your own effect type (Future, IO, ...), or use the `unsafe` API instead of the
    "safe `Identity` monad" to get the token. However, since the mechanism is the same as the one used for the Strava 
    API, you can refer to the [Usage](#usage) section for more information about such techniques.

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
alias for `SafeF[Identity, T]` (see the [Philosophy](#philosophy) section). This means that the response is either a
successful value `T` or a sttp exception.

### If you like playing with fire

Depending on your needs and situation, you may not want to be burdened by this type of `Either` and opt for a more
"Java-like" approach (no offense). In this case, it's very simple: replace the last two `val`'s above with:

```scala
val unsafeActivityApi: ActivityApi[UnsafeIdentity] =
  syncStravaClient.unsafeApi.activity

val unsafeActivities: UnsafeIdentity[List[SummaryActivity]] =
  unsafeActivityApi.getLoggedInAthleteActivities()
```  

Since `UnsafeIdentity[T]` is an alias for `T`, the response is always a successful value `T`, and your program will
throw an exception if something goes wrong. This is not recommended, but it's up to you.

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

val activitiesFuture: SafeFuture[List[SummaryActivity]] =
  futureStravaClient.api.activity.getLoggedInAthleteActivities()

activitiesFuture.onComplete {
  case Success(Right(activities: List[SummaryActivity])) =>
    activities.foreach(println)
  case Success(Left(error: ResponseException[Fault, circe.Error])) =>
    println(error)
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

## Bonus: minimal HTTP server catching the authorization code

Here is a minimal example of how to catch the authorization code used to obtain an access token using 
[http4s](https://http4s.org/) and [cats-effect](https://typelevel.org/cats-effect/). First, add the following 
dependencies to your `build.sbt`:
    
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