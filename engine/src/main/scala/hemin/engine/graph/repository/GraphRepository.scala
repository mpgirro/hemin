package hemin.engine.graph.repository

import com.typesafe.scalalogging.Logger
import hemin.engine.model.{Episode, Person, Podcast}

import scala.concurrent.{ExecutionContext, Future}


object GraphRepository {
  /** The Label of Podcast nodes */
  val PODCAST_LABEL: String = "Podcast"

  /** The Label of Episode nodes */
  val EPISODE_LABEL: String = "Episode"

  /** The Label of Website nodes */
  val WEBSITE_LABEL: String = "Website"

  /** The Label of Person nodes */
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

  /** Closes the connection to the database */
  def close(): Unit

  /** Drop all nodes of any kind */
  def dropAll(): Unit

  /** Creates a node for a Podcast
    *
    * @param podcast The Podcast to create the Node for
    * @return Eventually, nothing or an error
    */
  def createPodcast(podcast: Podcast): Future[Unit]

  /** Creates a node for an Episode
    *
    * @param episode The Episode to create the Node for
    * @return Eventually, nothing or an error
    */
  def createEpisode(episode: Episode): Future[Unit]

  /** Creates a node for a Website
    *
    * @param url The URL of the website to create the Node for
    * @return Eventually, nothing or an error
    */
  def createWebsite(url: String): Future[Unit]

  /** Creates a node for a Person
    *
    * @param person The Person to create the Node for
    * @return Eventually, nothing or an error
    */
  def createPerson(person: Person): Future[Unit]

  /** Creates a node for a Person by name only
    *
    * @param name The name of the Person to create the Node for
    * @return Eventually, nothing or an error
    */
  def createPerson(name: String): Future[Unit]

  /** Creates a relationship between a Podcast node and an Episode node
    *
    * @param podcastId The ID of the respective Podcast node
    * @param episodeId The ID of the respective Episode node
    * @return Eventually, nothing or an error
    */
  def linkPodcastEpisode(podcastId: String, episodeId: String): Future[Unit]

  /** Creates a relationship between a Podcast node and a Website node
    *
    * @param podcastId The ID of the respective Podcast node
    * @param url The URL of the respective Website node
    * @return Eventually, nothing or an error
    */
  def linkPodcastWebsite(podcastId: String, url: String): Future[Unit]

  /** Creates a relationship between a Podcast node and a Person node
    *
    * @param podcastId The ID of the respective Podcast node
    * @param person The Person object behind the respective Person node
    * @param role The role that is represented by the relationship
    * @return Eventually, nothing or an error
    */
  def linkPodcastPerson(podcastId: String, person: Person, role: String): Future[Unit]

  /** Creates a relationship between an Episode node and a Website node
    *
    * @param episodeId The ID of the respective Episode node
    * @param url The URL of the respective Website node
    * @return Eventually, nothing or an error
    */
  def linkEpisodeWebsite(episodeId: String, url: String): Future[Unit]

  /** Creates a relationship between an Episode node and a Person node
    *
    * @param episodeId The ID of the respective Episode node
    * @param person The Person object behind the respective Person node
    * @param role The role that is represented by the relationship
    * @return Eventually, nothing or an error
    */
  def linkEpisodePerson(episodeId: String, person: Person, role: String): Future[Unit]

  /** Creates a relationship between an Episode node and a Person node
    *
    * @param episodeId The ID of the respective Episode node
    * @param personName The name of the respective Person node
    * @param role The role that is represented by the relationship
    * @return Eventually, nothing or an error
    */
  def linkEpisodePerson(episodeId: String, personName: String, role: String): Future[Unit]

}
