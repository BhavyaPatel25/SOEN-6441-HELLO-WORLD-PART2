ThisBuild / scalaVersion := "2.13.15"

ThisBuild / version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    name := """reactivetubelytics_v2""",
    libraryDependencies ++= Seq(
      guice,
      // Database dependencies
      javaJdbc,
      ws,

      // Google API Client Library
      "com.google.api-client" % "google-api-client" % "1.23.0",

      // OAuth 2.0 Client Library
      "com.google.oauth-client" % "google-oauth-client-jetty" % "1.23.0",

      // YouTube Data API (ensure correct revision and version)
      "com.google.apis" % "google-api-services-youtube" % "v3-rev222-1.25.0",

      // OkHttp for HTTP requests (optional if needed)
      "com.squareup.okhttp3" % "okhttp" % "4.9.3",

      // Gson for JSON parsing (optional)
      "com.google.code.gson" % "gson" % "2.8.8",

      // Testing dependencies
      "org.mockito" % "mockito-core" % "5.0.0" % Test,
      "org.mockito" % "mockito-junit-jupiter" % "5.0.0" % Test,
      "org.junit.jupiter" % "junit-jupiter" % "5.10.0" % Test,
      "com.typesafe.akka" %% "akka-slf4j" % "2.6.20",
      "ch.qos.logback" % "logback-classic" % "1.2.11",
      "com.typesafe.akka" %% "akka-testkit" % "2.6.21" % Test,
      "com.google.apis" % "google-api-services-youtube" % "v3-rev222-1.25.0"
    )
  )
