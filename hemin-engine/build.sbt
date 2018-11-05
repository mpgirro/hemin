
// Note:
//   Blank lines need to separate the statements.
//   := means you are setting the value for that key
//   += means you are adding to the values for that key

organization := "io.hemin"
name := "hemin-engine"
version := "1.0-SNAPSHOT"

scalaVersion := "2.12.6"

sbtVersion := "1.1.6"

//akkaVersion := "2.5.11"

initialize := {
    assert(
        Integer.parseInt(sys.props("java.specification.version").split("\\.")(1))
            >= 8,
        "Java 8 or above required")
}

mainClass in run := Some("io.hemin.engine.EngineApp")

//fork in run := true

connectInput in run := true

outputStrategy := Some(StdoutOutput)

//Defaults.itSettings

resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
)

//val scalaVersion = "2.12.6" // TODO extract stuff from above
val akkaVersion = "2.5.11"
val luceneVersion = "7.5.0"
val romeVersion = "1.9.0"
val sttpVersion = "1.1.10"
val scrimageVersion = "2.1.8"

libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.12.6"
libraryDependencies += "org.scala-lang" % "scala-library" % "2.12.6"
libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0"       // https://github.com/scala/scala-java8-compat
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0"        // https://github.com/lightbend/scala-logging
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"                   // https://github.com/qos-ch/logback
libraryDependencies += "org.reactivemongo" %% "reactivemongo" % "0.16.0"                // https://github.com/ReactiveMongo/ReactiveMongo
libraryDependencies += "org.apache.lucene" % "lucene-core" % luceneVersion              // https://github.com/apache/lucene-solr
libraryDependencies += "org.apache.lucene" % "lucene-queryparser" % luceneVersion
libraryDependencies += "org.apache.lucene" % "lucene-analyzers-common" % luceneVersion
libraryDependencies += "org.apache.solr" % "solr-solrj" % luceneVersion
libraryDependencies += "org.jsoup" % "jsoup" % "1.11.2"                                 // https://github.com/jhy/jsoup
libraryDependencies += "com.google.code.gson" % "gson" % "2.8.2"                        // https://github.com/google/gson
libraryDependencies += "com.google.guava" % "guava" % "24.1.1-jre"                      // https://github.com/google/guava
libraryDependencies += "com.rometools" % "rome" % romeVersion                           // https://github.com/rometools/rome
libraryDependencies += "com.rometools" % "rome-modules" % romeVersion
libraryDependencies += "org.hashids" % "hashids" % "1.0.3"                              // https://github.com/10cella/hashids-java
libraryDependencies += "com.softwaremill.sttp" %% "core" % sttpVersion                  // https://github.com/softwaremill/sttp
libraryDependencies += "com.softwaremill.sttp" %% "akka-http-backend" % sttpVersion
libraryDependencies += "com.lihaoyi" %% "pprint" % "0.5.3"                              // http://www.lihaoyi.com/PPrint/
libraryDependencies += "com.beachape" %% "enumeratum" % "1.5.13"                        // https://github.com/lloydmeta/enumeratum
libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-core" % scrimageVersion     // https://github.com/sksamuel/scrimage
libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-filters" % scrimageVersion
libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-io-extra" % scrimageVersion
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test                  // https://github.com/scalatest/scalatest
libraryDependencies += "junit" % "junit" % "4.12" % Test                // not sure why I would need this
libraryDependencies += "org.pegdown" % "pegdown" % "1.6.0" % Test // In addition to your testCompile dependency on scalatest, you also require a testRuntime dependency on pegdown in order to create the HTML report.
libraryDependencies += "com.github.simplyscala" %% "scalatest-embedmongo" % "0.2.4" % Test  // https://github.com/SimplyScala/scalatest-embedmongo


// Initial commands to be run in your REPL.  I like to import various project-specific things here.
initialCommands := """
    import io.hemin.engine._;
  """