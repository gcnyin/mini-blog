ThisBuild / scalaVersion := "2.13.7"
ThisBuild / version := "0.1"
ThisBuild / organization := "com.github.gcnyin"
ThisBuild / organizationName := "mini-blog"

val tapirVersion = "0.19.3"
val akkaVersion = "2.6.18"
val macwireVersion = "2.5.2"
val zioVersion = "1.0.13"
val zioLoggingVersion = "0.5.14"
val circeVersion = "0.14.1"

lazy val root = (project in file("."))
  .settings(
    name := "mini-blog",
    libraryDependencies ++= Seq(
      // api
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
      // dj
      "dev.zio" %% "zio" % zioVersion,
      // http
      "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % tapirVersion,
      "de.heikoseeberger" %% "akka-http-circe" % "1.39.2",
      "com.typesafe.akka" %% "akka-http" % "10.2.7",
      // html
      "com.lihaoyi" %% "scalatags" % "0.11.0",
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
      "org.reactivemongo" %% "reactivemongo" % "1.0.10",
      // logging
      "ch.qos.logback" % "logback-classic" % "1.2.10",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
      "commons-logging" % "commons-logging" % "1.2", // because of spring-security-crypto
      "dev.zio" %% "zio-logging" % zioLoggingVersion,
      "dev.zio" %% "zio-logging-slf4j" % zioLoggingVersion,
      // test
      "com.softwaremill.macwire" %% "macros" % macwireVersion % Test,
      "com.softwaremill.macwire" %% "util" % macwireVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.2.10" % Test,
      "org.scalatestplus" %% "mockito-3-4" % "3.2.10.0" % Test
    ),
    scalacOptions ++= Seq(
      "-deprecation"
    ),
    coverageEnabled := true
  )
  .enablePlugins(JavaAppPackaging)
