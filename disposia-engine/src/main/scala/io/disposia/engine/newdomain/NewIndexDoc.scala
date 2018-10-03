package io.disposia.engine.newdomain

import java.time.LocalDateTime


case class NewIndexDoc(
                        docType: Option[String] = None,
                        id: Option[String] = None,
                        title: Option[String] = None,
                        link: Option[String] = None,
                        description: Option[String] = None,
                        pubDate: Option[LocalDateTime] = None,
                        image: Option[String] = None,
                        itunesAuthor: Option[String] = None,
                        itunesSummary: Option[String] = None,
                        podcastTitle: Option[String] = None,
                        chapterMarks: Option[String] = None,
                        contentEncoded: Option[String] = None,
                        transcript: Option[String] = None,
                        websiteData: Option[String] = None
                      )
