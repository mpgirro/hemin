<h1 align="center">
  HEMIN
</h1>

<h3 align="center">
  Podcast Catalog & Search Engine System
</h3>

<div align="center">
  
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/93fa2bfafa7f47988bb01dee6a50add5)](https://app.codacy.com/app/mpgirro/hemin?utm_source=github.com&utm_medium=referral&utm_content=mpgirro/hemin&utm_campaign=Badge_Grade_Dashboard)
[![Website hemin.io](https://img.shields.io/website-up-down-green-red/https/hemin.io.svg)](https://hemin.io/) [![Build Status](https://travis-ci.org/mpgirro/hemin.png)](https://travis-ci.org/mpgirro/hemin) [![Scala version](https://img.shields.io/badge/scala-2.12-blue.svg)](https://www.scala-lang.org/download/2.12.0.html) [![Elm version](https://img.shields.io/badge/elm-0.19-blue.svg)](https://github.com/elm/compiler/blob/master/upgrade-docs/0.19.md) [![Akka version](https://img.shields.io/badge/akka-2.5-blue.svg)](https://akka.io/blog/news/2017/04/13/akka-2.5.0-released) [![Play Framework version](https://img.shields.io/badge/play-2.6-blue.svg)](https://www.playframework.com/documentation/2.6.x/Highlights26) [![Solr version](https://img.shields.io/badge/solr-7.5-blue.svg)](https://lucene.apache.org/solr/guide/7_5/index.html) [![Mongo version](https://img.shields.io/badge/mongo-4.0-blue.svg)](https://docs.mongodb.com/manual/release-notes/4.0/)

</div>



<!--[![GitHub release](https://img.shields.io/github/release/mpgirro/hemin.svg)](https://github.com/mpgirro/hemin/releases/)-->


HEMIN is a podcast catalog & search engine infrastructure. It's engine is written in [Scala](https://www.scala-lang.org), uses [MongoDB](https://www.mongodb.com) to store the podcast/episode/feed catalog, and indexes the data with [Solr](http://lucene.apache.org/solr/). The REST API relies on the [Play](https://www.playframework.com) framework for routing and JSON. The server is fully asynchronous, and makes heavy use of [Akka](https://akka.io) actors and [Scala Futures](https://docs.scala-lang.org/overviews/core/futures.html). The current web client is written in ~~[Typescript](https://www.typescriptlang.org) and builds on the [Angular](https://angular.io) framework~~ [Elm](https://elm-lang.org).


### ⚠️ Early development status warning


The Hemin infrastructure is still in the early development phase and not yet ready for full productive use.

The system currently supports the following standards:

* Syndication feeds in [RSS 2.0](https://cyber.harvard.edu/rss/rss.html) or Atom ([RFC 4287](https://tools.ietf.org/html/rfc4287)) format
* Metadata from Apple's [Podcasts Connect](https://help.apple.com/itc/podcasts_connect/#/itcb54353390) XML namespace ("iTunes tags")
* Chapter marks in [Podlove Simple Chapter](https://podlove.org/simple-chapters/) format

Support for the following standards is planned:

* Paged Feeds ([RFC 5005](https://tools.ietf.org/html/rfc5005))
* Add feeds by importing [OPML](http://dev.opml.org) files
* Provide an [OpenAPI specification](https://github.com/OAI/OpenAPI-Specification) for [API v1](docs/api/v1.md)
* Adhere to [JSON:API](https://jsonapi.org) standards in [API v2](docs/api/v2.md)
* Provide an [OpenAPI specification](https://github.com/OAI/OpenAPI-Specification) for [API v2](docs/api/v2.md)

Further roadmap information for the subprojects is documented in their respective [GitHub projects](https://github.com/mpgirro/hemin/projects) board.


## WebApp ([hemin.io](https://hemin.io))


A WebApp is available at [hemin.io](https://hemin.io)

There are several different frontend variant implementations for the Hemin system. The webapps are named after the letters in the greek alphabet. Currently there are these:

* _Alpha_ &ndash; based on [Angular](https://angular.io). An early proof-of-concept UI back from when Hemin was called something else and it's engine served as a scientific guinea pig (more about those days [here](https://github.com/mpgirro/dipl)). Development on Alpha is discontinued. <!--The API compatibility is outdated. -->
* [Beta](beta) &ndash; written in [Elm](https://elm-lang.org). It is still work in progress and quite *beta* (pun intended), but will become the production implementation.
<!--
* [Gamma](web/gamma) &ndash; proposition of a [React](https://reactjs.org) based implementation.  
* [Delta](web/delta) &ndash; proposition of a [VueJS](https://vuejs.org) based implementation. 
-->


## RESTful API ([api.hemin.io](https://api.hemin.io))


The REST endpoint to the API is [api.hemin.io](https://api.hemin.io)


## Engine 


The Hemin engine can be started as a standalone command line application that feature a REPL. Alternatively, it can be used embedded within another Scala/Java (or other compatible JVM language) application. This second way is how the API server integrates the engine.
