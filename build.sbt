ThisBuild / scalaVersion := "3.6.2"
ThisBuild / organization := "be.brkaisin"
ThisBuild / version := "0.0.1"
ThisBuild / organizationName := "Brieuc Kaisin"

lazy val root = (project in file("."))
  .settings(
    name := "scala-strava-sttp",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client4" %% "core" % "4.0.0-M22",
      "com.softwaremill.sttp.client4" %% "circe" % "4.0.0-M22",
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-feature",
      "-unchecked",
      "-Xfatal-warnings",
      "-Wvalue-discard",
      "-Xmax-inlines",
      "100"
    ),
    Test / parallelExecution := false,
    fork / run := true
  )
