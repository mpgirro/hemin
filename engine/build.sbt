
// Note:
//   Blank lines need to separate the statements.
//   := means you are setting the value for that key
//   += means you are adding to the values for that key

organization := "io.hemin"
name := "hemin-engine"
version := "1.0-SNAPSHOT"

scalaVersion := "2.12.6"

sbtVersion := "1.1.6"

initialize := {
    assert(
        Integer.parseInt(sys.props("java.specification.version").split("\\.")(1))
            >= 8,
        "Java 8 or above required")
}

mainClass in run := Some("hemin.engine.HeminApp")

//fork in run := true

connectInput in run := true

outputStrategy := Some(StdoutOutput)

parallelExecution in Test := false

//Defaults.itSettings

resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
)

//val scalaVersion = "2.12.6" // TODO extract stuff from above
val akkaVersion = "2.5.17"
val solrVersion = "7.5.0"
val romeVersion = "1.12.0"
val sttpVersion = "1.4.2"
val scrimageVersion = "2.1.8"

libraryDependencies ++= Seq(
    "org.scala-lang"             %  "scala-compiler"          % "2.12.6",        // https://github.com/scala/scala
    "org.scala-lang"             %  "scala-library"           % "2.12.6",
    "org.scala-lang.modules"     %% "scala-java8-compat"      % "0.8.0",         // https://github.com/scala/scala-java8-compat
    "com.typesafe.scala-logging" %% "scala-logging"           % "3.9.0",         // https://github.com/lightbend/scala-logging
    "com.typesafe.akka"          %% "akka-actor"              % akkaVersion,
    "com.typesafe.akka"          %% "akka-slf4j"              % akkaVersion,
    "com.typesafe.akka"          %% "akka-stream"             % akkaVersion,
    "com.typesafe.akka"          %% "akka-cluster"            % akkaVersion,
    "com.typesafe.akka"          %% "akka-cluster-tools"      % akkaVersion,
    "com.typesafe.akka"          %% "akka-testkit"            % akkaVersion % Test,
    "ch.qos.logback"             %  "logback-classic"         % "1.2.3",         // https://github.com/qos-ch/logback
    "org.reactivemongo"          %% "reactivemongo"           % "0.16.0",        // https://github.com/ReactiveMongo/ReactiveMongo
    "org.apache.solr"            %  "solr-solrj"              % solrVersion,     // https://github.com/apache/lucene-solr
    "org.jsoup"                  %  "jsoup"                   % "1.11.3",        // https://github.com/jhy/jsoup
    "com.google.code.gson"       %  "gson"                    % "2.8.5",         // https://github.com/google/gson
    "com.google.guava"           %  "guava"                   % "27.0-jre",      // https://github.com/google/guava
    "com.rometools"              %  "rome"                    % romeVersion,     // https://github.com/rometools/rome
    "com.rometools"              %  "rome-opml"               % romeVersion,
    "com.rometools"              %  "rome-modules"            % romeVersion,
    "org.hashids"                %  "hashids"                 % "1.0.3",         // https://github.com/10cella/hashids-java
    "com.softwaremill.sttp"      %% "core"                    % sttpVersion,     // https://github.com/softwaremill/sttp
    "com.softwaremill.sttp"      %% "akka-http-backend"       % sttpVersion,
    "com.lihaoyi"                %% "pprint"                  % "0.5.3",         // http://www.lihaoyi.com/PPrint/
    "com.beachape"               %% "enumeratum"              % "1.5.13",        // https://github.com/lloydmeta/enumeratum
    "com.sksamuel.scrimage"      %% "scrimage-core"           % scrimageVersion, // https://github.com/sksamuel/scrimage
    "com.sksamuel.scrimage"      %% "scrimage-filters"        % scrimageVersion,
    "com.sksamuel.scrimage"      %% "scrimage-io-extra"       % scrimageVersion,
    "org.neo4j.driver"           %  "neo4j-java-driver"       % "1.7.2",
    "org.rogach"                 %% "scallop"                 % "3.1.5",         // https://github.com/scallop/scallop
    "org.scalatest"              %% "scalatest"               % "3.0.5" % Test,  // https://github.com/scalatest/scalatest
    "junit"                      %  "junit"                   % "4.12"  % Test,  // TODO not sure why I would need this
    "org.pegdown"                %  "pegdown"                 % "1.6.0" % Test,  // In addition to your testCompile dependency on scalatest, you also require a testRuntime dependency on pegdown in order to create the HTML report.
)

// Initial commands to be run in your REPL.  I like to import various project-specific things here.
initialCommands := """
    import hemin.engine._;
  """