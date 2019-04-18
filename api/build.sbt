import sbt.Keys._

organization := "io.hemin"
name := "hemin-api"
version := "1.0-SNAPSHOT"

lazy val GatlingTest = config("gatling") extend Test

scalaVersion in ThisBuild := "2.12.6"

crossScalaVersions := Seq("2.11.12", "2.12.6")

resolvers ++= Seq(
    Resolver.mavenLocal,
    Resolver.jcenterRepo,
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
)

def gatlingVersion(scalaBinVer: String): String = scalaBinVer match {
  case "2.11" => "2.2.5"
  case "2.12" => "2.3.1"
}

// Note: This version must exactly match the one used by the Hemin engine
val akkaVersion = "2.5.22"
val akkaHttpVersion = "10.1.8"

// Akka dependencies used by Play
libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor"     % akkaVersion,
    "com.typesafe.akka" %% "akka-stream"    % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j"     % akkaVersion,
    "com.typesafe.akka" %% "akka-http"      % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-parsing"   % akkaHttpVersion
)

libraryDependencies += guice

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.7.3"  // https://github.com/playframework/play-json

libraryDependencies += "io.hemin" %% "hemin-engine" % "1.0-SNAPSHOT"

libraryDependencies += "org.joda" % "joda-convert" % "1.9.2"
libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "4.11"

libraryDependencies += "com.netaporter" %% "scala-uri" % "0.4.16"
libraryDependencies += "net.codingwell" %% "scala-guice" % "4.2.1"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion(scalaBinaryVersion.value) % Test
libraryDependencies += "io.gatling" % "gatling-test-framework" % gatlingVersion(scalaBinaryVersion.value) % Test

// JAX-RS is required by Swagger for some annotations
libraryDependencies += "javax.ws.rs" % "javax.ws.rs-api" % "2.1.1" artifacts(Artifact("javax.ws.rs-api", "jar", "jar")) // this is a workaround for https://github.com/jax-rs/api/issues/571
libraryDependencies += "io.swagger" %% "swagger-play2" % "1.7.0"
libraryDependencies += "com.iheart" %% "play-swagger" % "0.7.5-PLAY2.7" // https://github.com/iheartradio/play-swagger

//libraryDependencies += "org.zalando" %% "scala-jsonapi" % "0.6.2" // TODO currently only supports Scala 2.11

//swaggerDomainNameSpaces := Seq("hemin.engine.model")

// The Play project itself
lazy val root = (project in file("."))
  .enablePlugins(Common, PlayScala, GatlingPlugin)
  .configs(GatlingTest)
  .settings(inConfig(GatlingTest)(Defaults.testSettings): _*)
  .settings(
    name := """hemin-api""",
    scalaSource in GatlingTest := baseDirectory.value / "/gatling/simulation"
  )


