package io.disposia.engine.newdomain

case class NewChapter(
                       id: Option[String] = None,
                       episodeId: Option[String] = None,
                       start: Option[String] = None,
                       title: Option[String] = None,
                       href: Option[String] = None,
                       image: Option[String] = None
                     )
