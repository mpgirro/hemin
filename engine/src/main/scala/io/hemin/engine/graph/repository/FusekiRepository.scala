package io.hemin.engine.graph.repository

import com.typesafe.scalalogging.Logger
import io.hemin.engine.model.{Episode, Person, Podcast}

import scala.concurrent.{ExecutionContext, Future}

class FusekiRepository extends GraphRepository {

  override protected[this] implicit val executionContext: ExecutionContext =
    throw new UnsupportedOperationException("FusekiRepository.executionContext")

  override protected[this] val log: Logger =
    throw new UnsupportedOperationException("FusekiRepository.log")

  /** Closes the connection to the database */
  override def close(): Unit =
    throw new UnsupportedOperationException("FusekiRepository.close()")

  /** Drop all nodes of any kind */
  override def dropAll(): Unit =
    throw new UnsupportedOperationException("FusekiRepository.dropAll()")

  /** Creates a node for a Podcast
    *
    * @param podcast The Podcast to create the Node for
    * @return Eventually, nothing or an error
    */
  override def createPodcast(podcast: Podcast): Future[Unit] =
    throw new UnsupportedOperationException("FusekiRepository.createPodcast(Podcast)")

  /** Creates a node for an Episode
    *
    * @param episode The Episode to create the Node for
    * @return Eventually, nothing or an error
    */
  override def createEpisode(episode: Episode): Future[Unit] =
    throw new UnsupportedOperationException("FusekiRepository.createEpisode(Episode)")

  /** Creates a node for a Website
    *
    * @param url The URL of the website to create the Node for
    * @return Eventually, nothing or an error
    */
  override def createWebsite(url: String): Future[Unit] =
    throw new UnsupportedOperationException("FusekiRepository.createWebsite(String)")

  /** Creates a node for a Person
    *
    * @param person The Person to create the Node for
    * @return Eventually, nothing or an error
    */
  override def createPerson(person: Person): Future[Unit] =
    throw new UnsupportedOperationException("FusekiRepository.createPerson(Person)")

  /** Creates a node for a Person by name only
    *
    * @param name The name of the Person to create the Node for
    * @return Eventually, nothing or an error
    */
  override def createPerson(name: String): Future[Unit] =
    throw new UnsupportedOperationException("FusekiRepository.createPerson(String)")

  /** Creates a relationship between a Podcast node and an Episode node
    *
    * @param podcastId The ID of the respective Podcast node
    * @param episodeId The ID of the respective Episode node
    * @return Eventually, nothing or an error
    */
  override def linkPodcastEpisode(podcastId: String, episodeId: String): Future[Unit] =
    throw new UnsupportedOperationException("FusekiRepository.linkPodcastEpisode(String,String)")

  /** Creates a relationship between a Podcast node and a Website node
    *
    * @param podcastId The ID of the respective Podcast node
    * @param url       The URL of the respective Website node
    * @return Eventually, nothing or an error
    */
  override def linkPodcastWebsite(podcastId: String, url: String): Future[Unit] =
    throw new UnsupportedOperationException("FusekiRepository.linkPodcastWebsite(String,String)")

  /** Creates a relationship between a Podcast node and a Person node
    *
    * @param podcastId The ID of the respective Podcast node
    * @param person    The Person object behind the respective Person node
    * @param role      The role that is represented by the relationship
    * @return Eventually, nothing or an error
    */
  override def linkPodcastPerson(podcastId: String, person: Person, role: String): Future[Unit] =
    throw new UnsupportedOperationException("FusekiRepository.linkPodcastPerson(String,Person,String)")

  /** Creates a relationship between an Episode node and a Website node
    *
    * @param episodeId The ID of the respective Episode node
    * @param url       The URL of the respective Website node
    * @return Eventually, nothing or an error
    */
  override def linkEpisodeWebsite(episodeId: String, url: String): Future[Unit] =
    throw new UnsupportedOperationException("FusekiRepository.linkEpisodeWebsite(String,String)")

  /** Creates a relationship between an Episode node and a Person node
    *
    * @param episodeId The ID of the respective Episode node
    * @param person    The Person object behind the respective Person node
    * @param role      The role that is represented by the relationship
    * @return Eventually, nothing or an error
    */
  override def linkEpisodePerson(episodeId: String, person: Person, role: String): Future[Unit] =
    throw new UnsupportedOperationException("FusekiRepository.linkEpisodePerson(String,Person,String)")

  /** Creates a relationship between an Episode node and a Person node
    *
    * @param episodeId  The ID of the respective Episode node
    * @param personName The name of the respective Person node
    * @param role       The role that is represented by the relationship
    * @return Eventually, nothing or an error
    */
  override def linkEpisodePerson(episodeId: String, personName: String, role: String): Future[Unit] =
    throw new UnsupportedOperationException("FusekiRepository.linkEpisodePerson(String,String,String)")
}
