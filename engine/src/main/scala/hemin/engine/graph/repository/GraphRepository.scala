package hemin.engine.graph.repository

import com.typesafe.scalalogging.Logger
import hemin.engine.model.{Episode, Person, Podcast}

import scala.concurrent.{ExecutionContext, Future}


object GraphRepository {
  val PODCAST_LABEL: String = "Podcast"
  val EPISODE_LABEL: String = "Episode"
  val WEBSITE_LABEL: String = "Website"
  val PERSON_LABEL: String = "Person"

  val AUTHOR_RELATIONSHIP: String = "AUTHOR"
  val BELONGS_TO_RELATIONSHIP: String = "BELONGS_TO"
  val CONTRIBUTOR_RELATIONSHIP: String = "CONTRIBUTOR"
  val ITUNES_AUTHOR_RELATIONSHIP: String = "ITUNES_AUTHOR"
  val ITUNES_OWNER_RELATIONSHIP: String = "ITUNES_OWNER"
  val LINKS_TO_RELATIONSHIP: String = "LINKS_TO"
}

trait GraphRepository {

  protected[this] implicit val executionContext: ExecutionContext

  protected[this] val log: Logger

  def close(): Unit

  def dropAll(): Unit

  def createPodcast(podcast: Podcast): Future[Unit]

  def createEpisode(episode: Episode): Future[Unit]

  def createWebsite(url: String): Future[Unit]

  def createPerson(person: Person): Future[Unit]

  def createPerson(name: String): Future[Unit]

  def linkPodcastEpisode(podcastId: String, episodeId: String): Future[Unit]

  def linkPodcastWebsite(podcastId: String, url: String): Future[Unit]

  def linkPodcastPerson(podcastId: String, person: Person, role: String): Future[Unit]

  def linkEpisodeWebsite(episodeId: String, url: String): Future[Unit]

  def linkEpisodePerson(podcastId: String, person: Person, role: String): Future[Unit]

  def linkEpisodePerson(podcastId: String, personName: String, role: String): Future[Unit]

}
