package io.hemin.engine.parser.feed

import io.hemin.engine.model.{Episode, Podcast}

/**
  *
  * @param podcast The Podcast model holding the parsing result values
  * @param episodes List of all Episode models holding the parsing result values
  */
case class FeedParserResult (
  podcast: Podcast,
  episodes: List[Episode],
)
