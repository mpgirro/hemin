package hemin.engine.parser.feed

import hemin.engine.model.{Episode, Podcast}

trait FeedParser {

  /** The Podcast model holding the parsing result values */
  val podcast: Podcast

  /** List of all Episode models holding the parsing result values */
  val episodes: List[Episode]

}
