
# Hemin : Podcast Catalog & Search Engine System


[![Website hemin.io](https://img.shields.io/website-up-down-green-red/https/hemin.io.svg)](https://hemin.io/)
[![GitHub release](https://img.shields.io/github/release/mpgirro/hemin.svg)](https://github.com/mpgirro/hemin/releases/)
[![Twitter](https://img.shields.io/badge/Twitter-%40hemin_io-blue.svg)](https://twitter.com/hemin_io)


:heavy_exclamation_mark: __DISCLAIMER__: The Hemin infrastructure is still in the initial development phase and not yet ready for full productive use.


Hemin is a podcast catalog & search engine infrastructure. It's engine is written in [Scala](https://www.scala-lang.org) (with a small compatibility layer in Java), uses [MongoDB](https://www.mongodb.com) to store the podcast/episode/feed catalog, and indexes the data with [Solr](http://lucene.apache.org/solr/). The REST API relies on the [Play](https://www.playframework.com) framework for routing and JSON. The server is fully asynchronous, and makes heavy use of [Akka](https://akka.io) actors and [Scala Futures](https://docs.scala-lang.org/overviews/core/futures.html). The web client is written in [Typescript](https://www.typescriptlang.org) and builds on the [Angular](https://angular.io) framework. 

The Hemin system supports the following standards:

* [Podlove Simple Chapter](https://podlove.org/simple-chapters/)

Support for the following standards is planned:

* Pages Feeds ([RFC 5005](https://tools.ietf.org/html/rfc5005))
* [JSON:API](https://jsonapi.org)


## WebApp


[![Node version](https://img.shields.io/badge/node-9.11-blue.svg)](https://nodejs.org/en/blog/release/v9.11.2/)
[![Angular version](https://img.shields.io/badge/angular-5-blue.svg)](https://blog.angular.io/version-5-0-0-of-angular-now-available-37e414935ced)


The WebApp is available at [hemin.io](https://hemin.io)


## REST API


[![Play Framework version](https://img.shields.io/badge/play-2.6-blue.svg)](https://www.playframework.com/documentation/2.6.x/Highlights26)


The REST endpoint to the API is [api.hemin.io](https://api.hemin.io)


## Engine 


[![Scala version](https://img.shields.io/badge/scala-2.12-blue.svg)](https://www.scala-lang.org/download/2.12.0.html)
[![Java version](https://img.shields.io/badge/java-1.8-blue.svg)](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
[![Akka version](https://img.shields.io/badge/akka-2.5-blue.svg)](https://akka.io/blog/news/2017/04/13/akka-2.5.0-released)
[![Solr version](https://img.shields.io/badge/solr-7.5-blue.svg)](https://lucene.apache.org/solr/guide/7_5/index.html)
[![Mongo version](https://img.shields.io/badge/mongo-4.0-blue.svg)](https://docs.mongodb.com/manual/release-notes/4.0/)


The Hemin engine can be started as a standalone command line application that feature a REPL. Alternatively, it can be used embedded within another Scala/Java (or other compatible JVM language) application. This second way is how the Hemin API server integrates the Hemin engine.
