package hemin.engine.semantic.repository.graph

import com.typesafe.scalalogging.Logger
import hemin.engine.semantic.SemanticConfig
import hemin.engine.semantic.repository.graph.GraphRepository._
import hemin.engine.model.{Episode, Person, Podcast}
import org.neo4j.driver.v1._

import scala.concurrent.{ExecutionContext, Future}

class Neo4jRepository(config: SemanticConfig,
                      ec: ExecutionContext)
  extends GraphRepository {

  override protected[this] val log: Logger = Logger(getClass)

  override protected[this] implicit val executionContext: ExecutionContext = ec

  private val driver: Driver = GraphDatabase.driver(config.neo4jUri, AuthTokens.basic(config.username, config.password))
  private val session: Session = driver.session

  runScript(s"CREATE INDEX ON :$PODCAST_LABEL(id)")
  runScript(s"CREATE INDEX ON :$EPISODE_LABEL(id)")
  runScript(s"CREATE INDEX ON :$WEBSITE_LABEL(url)")
  runScript(s"CREATE INDEX ON :$PERSON_LABEL(name, email, url)")

  private def runScript(script: String): StatementResult = {
    session.run(script)
  }

  override def close(): Unit = {
    log.debug("Stopping the Neo4jDriver...")
    driver.close()
  }

  override def dropAll(): Unit = {
    val script =
    s"""MATCH (n)
       |DETACH DELETE n
       """.stripMargin
    runScript(script)
  }

  override def createPodcast(podcast: Podcast): Future[Unit] = Future {
    val id = podcast.id.getOrElse("")
    val title = podcast.title.getOrElse("")
    val properties = s"id: '$id', title: '$title'"
    val script =
    s"""MERGE (podcast:$PODCAST_LABEL{ id: '$id' })
       |ON CREATE SET podcast = { $properties }
       |ON MATCH  SET podcast += { $properties }
       """.stripMargin
    runScript(script)
  }

  override def createEpisode(episode: Episode): Future[Unit] = Future {
    val id = episode.id.getOrElse("")
    val title = episode.title.getOrElse("")
    val properties = s"id: '$id', title: '$title'"
    val script =
      s"""MERGE (episode:$EPISODE_LABEL{ id: '$id' })
         |ON CREATE SET episode = { $properties }
         |ON MATCH  SET episode += { $properties }
       """.stripMargin
    runScript(script)
  }

  override def createWebsite(url: String): Future[Unit] = Future {
    val properties = s"url: '$url'"
    val script =
      s"""MERGE (website:$WEBSITE_LABEL{ url: '$url' })
         |ON CREATE SET website = { $properties }
         |ON MATCH  SET website += { $properties }
       """.stripMargin
    runScript(script)
  }

  override def createPerson(person: Person): Future[Unit] = Future {
    val name = person.name.getOrElse("")
    val email = person.email.getOrElse("")
    val uri = person.uri.getOrElse("")
    val properties = s"name: '$name', email: '$email', uri: '$uri'"
    val script =
      s"""MERGE (person:$PERSON_LABEL{ name: { $properties }.name, email: { $properties }.email, uri: { $properties }.uri })
         |ON CREATE SET person = { $properties }
         |ON MATCH  SET person += { $properties }
       """.stripMargin
    runScript(script)
  }

  override def createPerson(name: String): Future[Unit] = Future {
    val properties = s"name: '$name'"
    val script =
      s"""MERGE (person:$PERSON_LABEL{ name: { $properties }.name })
         |ON CREATE SET person = { $properties }
         |ON MATCH  SET person += { $properties }
       """.stripMargin
    runScript(script)
  }

  override def linkPodcastEpisode(podcastId: String, episodeId: String): Future[Unit] = Future {
    val script =
      s"""MATCH (podcast:$PODCAST_LABEL),(episode:$EPISODE_LABEL)
         |WHERE podcast.id = '$podcastId' AND episode.id = '$episodeId'
         |CREATE (podcast)-[r:$BELONGS_TO_RELATIONSHIP]->(episode)
       """.stripMargin
    runScript(script)
  }

  override def linkPodcastWebsite(podcastId: String, url: String): Future[Unit] = Future {
    val script =
      s"""MATCH (podcast:$PODCAST_LABEL),(website:$WEBSITE_LABEL)
         |WHERE podcast.id = '$podcastId' AND website.url = '$url'
         |CREATE (podcast)-[r:$LINKS_TO_RELATIONSHIP]->(website)
       """.stripMargin
    runScript(script)
  }

  override def linkPodcastPerson(podcastId: String, person: Person, role: String): Future[Unit] = Future {
    val name = person.name.getOrElse("")
    val email = person.email.getOrElse("")
    val uri = person.uri.getOrElse("")
    val script =
      s"""MATCH (podcast:$PODCAST_LABEL),(person:$PERSON_LABEL)
         |WHERE podcast.id = '$podcastId' AND person.name = '$name' AND person.email = '$email' AND person.uri = '$uri'
         |CREATE (podcast)-[r:$role]->(person)
       """.stripMargin
    runScript(script)
  }

  override def linkEpisodeWebsite(episodeId: String, url: String): Future[Unit] = Future {
    val script =
      s"""MATCH (episode:$EPISODE_LABEL),(website:$WEBSITE_LABEL)
         |WHERE episode.id = '$episodeId' AND website.url = '$url'
         |CREATE (episode)-[r:$LINKS_TO_RELATIONSHIP]->(website)
       """.stripMargin
    runScript(script)
  }

  override def linkEpisodePerson(episodeId: String, person: Person, role: String): Future[Unit] = Future {
    val name = person.name.getOrElse("")
    val email = person.email.getOrElse("")
    val uri = person.uri.getOrElse("")
    val script =
      s"""MATCH (episode:$EPISODE_LABEL),(person:$PERSON_LABEL)
         |WHERE episode.id = '$episodeId' AND person.name = '$name' AND person.email = '$email' AND person.uri = '$uri'
         |CREATE (episode)-[r:$role]->(person)
       """.stripMargin
    runScript(script)
  }

  override def linkEpisodePerson(episodeId: String, personName: String, role: String): Future[Unit] = Future {
    val script =
      s"""MATCH (episode:$EPISODE_LABEL),(person:$PERSON_LABEL)
         |WHERE episode.id = '$episodeId' AND person.name = '$personName'
         |CREATE (episode)-[r:$role]->(person)
       """.stripMargin
    runScript(script)
  }

}
