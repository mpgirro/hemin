package hemin.engine.graph.repository

import com.typesafe.scalalogging.Logger
import hemin.engine.model.{Episode, Person, Podcast}

import scala.concurrent.{ExecutionContext, Future}

trait GraphRepository {

  protected[this] implicit val executionContext: ExecutionContext

  protected[this] val log: Logger

  def close(): Unit

  def dropAll(): Unit

  def createPodcast(podcast: Podcast): Future[Unit]

  def createEpisode(episode: Episode): Future[Unit]

  def createWebsite(url: String): Future[Unit]

  def createPerson(person: Person): Future[Unit]

  def linkPodcastEpisode(podcastId: String, episodeId: String): Future[Unit]

  def linkPodcastWebsite(podcastId: String, url: String): Future[Unit]

  def linkEpisodeWebsite(episodeId: String, url: String): Future[Unit]

}
