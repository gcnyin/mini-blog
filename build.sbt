ThisBuild / scalaVersion := "2.13.7"
ThisBuild / version := "0.1"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

val tapirVersion = "0.19.0"
val akkaVersion = "2.6.17"

lazy val root = (project in file("."))
  .settings(
    name := "blog",
    libraryDependencies ++= Seq(
      // dependency injection
      "com.softwaremill.macwire" %% "macros" % "2.4.0" % Provided,
      "com.softwaremill.macwire" %% "util" % "2.4.0",
      // http
      "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
      "io.circe" %% "circe-generic" % "0.14.1",
      "de.heikoseeberger" %% "akka-http-circe" % "1.38.2",
      "com.typesafe.akka" %% "akka-http" % "10.2.7",
      // openapi
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % tapirVersion,
      // akka
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      // security
      "org.springframework.security" % "spring-security-crypto" % "5.6.0",
      "com.github.jwt-scala" %% "jwt-circe" % "9.0.2",
      // database
      "org.reactivemongo" %% "reactivemongo" % "1.0.7",
      // logging
      "ch.qos.logback" % "logback-classic" % "1.2.7",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
      "commons-logging" % "commons-logging" % "1.2",
      // test
      "org.scalatest" %% "scalatest" % "3.2.10" % Test,
    ),
    coverageEnabled := true,
  )
  .enablePlugins(JavaAppPackaging)
