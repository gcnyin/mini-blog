ThisBuild / scalaVersion := "2.13.7"
ThisBuild / version := "0.1"
ThisBuild / organization := "com.github.gcnyin"
ThisBuild / organizationName := "mini-blog"

val tapirVersion = "0.19.1"
val akkaVersion = "2.6.17"
val macwireVersion = "2.5.0"
val zioVersion = "1.0.12"
val zioLoggingVersion = "0.5.14"
val circeVersion = "0.14.1"

lazy val root = (project in file("."))
  .aggregate(cross.js, jvm)
  .settings(
    name := "mini-blog",
    publish := {},
    publishLocal := {},
  )

lazy val cross = crossProject(JSPlatform, JVMPlatform)
  .in(file("."))
  .settings(
    name := "cross",
    version := "0.1",
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion,
      "com.softwaremill.sttp.tapir" %%% "tapir-json-circe" % tapirVersion,
      "dev.zio" %%% "zio" % zioVersion,
      "dev.zio" %%% "zio-logging" % zioLoggingVersion,
    ),
  )
  .jvmSettings(
    name := "jvm",
    libraryDependencies ++= Seq(
      // http
      "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % tapirVersion,
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
      "org.reactivemongo" %% "reactivemongo" % "1.0.8",
      // logging
      "ch.qos.logback" % "logback-classic" % "1.2.7",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
      "commons-logging" % "commons-logging" % "1.2", // because of spring-security-crypto
      "dev.zio" %% "zio-logging-slf4j" % zioLoggingVersion,
      // test
      "com.softwaremill.macwire" %% "macros" % macwireVersion % Test,
      "com.softwaremill.macwire" %% "util" % macwireVersion % Test,
      "org.scalatest" %% "scalatest" % "3.2.10" % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
      "org.scalatestplus" %% "mockito-3-4" % "3.2.10.0" % Test
    ),
    coverageEnabled := true
  )
  .jsSettings(
    name := "js",
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.0.0",
      "com.softwaremill.sttp.tapir" %%% "tapir-sttp-client" % "0.19.1",
      "io.github.cquiroz" %%% "scala-java-time" % "2.2.0",
      "org.scala-js" %%% "scala-js-macrotask-executor" % "1.0.0",
      "dev.zio" %%% "zio-logging-jsconsole" % zioLoggingVersion,
    ),
    scalaJSUseMainModuleInitializer := true,
    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv(),
  )

val jvm = cross.jvm.enablePlugins(JavaAppPackaging)
