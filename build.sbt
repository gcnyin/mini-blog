ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version := "0.1"
ThisBuild / organization := "com.github.gcnyin"
ThisBuild / organizationName := "mini-blog"

lazy val root = (project in file("."))
  .settings(
    name := "mini-blog",
    libraryDependencies ++= Seq(
      // tapir
      "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % "0.20.1",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "0.20.1",
      // json
      "io.circe" %% "circe-core" % "0.14.1",
      "io.circe" %% "circe-generic" % "0.14.1",
      "io.circe" %% "circe-parser" % "0.14.1",
      // akka
      "com.typesafe.akka" %% "akka-actor-typed" % "2.6.18",
      "com.typesafe.akka" %% "akka-stream" % "2.6.18",
      "com.typesafe.akka" %% "akka-http" % "10.2.9",
      "com.typesafe.akka" %% "akka-serialization-jackson" % "2.6.18",
      "com.typesafe.akka" %% "akka-persistence-typed" % "2.6.18",
      "com.typesafe.akka" %% "akka-persistence-testkit" % "2.6.18" % Test,
      "com.lightbend.akka" %% "akka-persistence-jdbc" % "5.0.4",
      "com.typesafe.slick" %% "slick" % "3.3.3",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
      // mysql
      "mysql" % "mysql-connector-java" % "8.0.26",
      // logging
      "ch.qos.logback" % "logback-classic" % "1.2.11",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
    ),
    scalacOptions ++= Seq(
      "-deprecation"
    )
  )
  .enablePlugins(JavaAppPackaging, JavaAgent)
  .settings(
    javaAgents += "io.opentelemetry.javaagent" % "opentelemetry-javaagent" % "1.12.0"
  )
