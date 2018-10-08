package io.disposia.engine.cnc

import io.disposia.engine.domain._

object CliFormatter {

  def format(podcast: Podcast): String = pprint.apply(podcast).toString()

  def format(episode: Episode): String = pprint.apply(episode).toString()

  def format(feed: Feed): String = pprint.apply(feed).toString()

  def format(chapter: Chapter): String = pprint.apply(chapter).toString()

  def format(image: Image): String = pprint.apply(image).toString()

  def format(indexDoc: IndexDoc): String = pprint.apply(indexDoc).toString()

  def format(results: ResultsWrapper): String = pprint.apply(results).toString()

  def format(xs: List[Any]): String = pprint.apply(xs).toString()

}
