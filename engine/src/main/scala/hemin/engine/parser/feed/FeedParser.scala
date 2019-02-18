package hemin.engine.parser.feed

import hemin.engine.model.{Episode, Podcast}

trait FeedParser {

  val podcast: Podcast

  val episodes: List[Episode]

}
