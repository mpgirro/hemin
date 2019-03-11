package hemin.engine.graph.repository

import com.typesafe.scalalogging.Logger
import hemin.engine.graph.GraphConfig
import hemin.engine.model.{Episode, Person, Podcast}
import org.neo4j.driver.v1._

import scala.concurrent.{ExecutionContext, Future}

class Neo4jRepository(config: GraphConfig,
                      ec: ExecutionContext)
  extends GraphRepository {

  override protected[this] val log: Logger = Logger(getClass)

  override protected[this] implicit val executionContext: ExecutionContext = ec

  private val podcastLabel: String = "Podcast"
  private val episodeLabel: String = "Episode"
  private val websiteLabel: String = "Website"
  private val personLabel: String = "Person"

  private val driver: Driver = GraphDatabase.driver(config.neo4jUri, AuthTokens.basic(config.username, config.password))
  private val session: Session = driver.session

  runScript(s"CREATE INDEX ON :$podcastLabel(id)")
  runScript(s"CREATE INDEX ON :$episodeLabel(id)")
  runScript(s"CREATE INDEX ON :$websiteLabel(url)")
  runScript(s"CREATE INDEX ON :$personLabel(name, email, url)")

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
    s"""MERGE (p:$podcastLabel{ id: '$id' })
       |ON CREATE SET n = { $properties }
       |ON MATCH  SET n += { $properties }
       """.stripMargin
    runScript(script)
  }

  override def createEpisode(episode: Episode): Future[Unit] = Future {
    val id = episode.id.getOrElse("")
    val title = episode.title.getOrElse("")
    val properties = s"id: '$id', title: '$title'"
    val script =
      s"""MERGE (e:$episodeLabel{ id: '$id' })
         |ON CREATE SET n = { $properties }
         |ON MATCH  SET n += { $properties }
       """.stripMargin
    runScript(script)
  }

  override def createWebsite(url: String): Future[Unit] = Future {
    val properties = s"url: '$url'"
    val script =
      s"""MERGE (w:$websiteLabel{ url: '$url' })
         |ON CREATE SET n = { $properties }
         |ON MATCH  SET n += { $properties }
       """.stripMargin
    runScript(script)
  }

  override def createPerson(person: Person): Future[Unit] = Future {
    val name = person.name.getOrElse("")
    val email = person.email.getOrElse("")
    val uri = person.uri.getOrElse("")
    val properties = s"name: '$name', email: '$email', uri: '$uri'"
    val script =
      s"""MERGE (p:$personLabel{ name: { $properties }.name, email: { $properties }.email, uri: { $properties }.uri })
         |ON CREATE SET n = { $properties }
         |ON MATCH  SET n += { $properties }
       """.stripMargin
    runScript(script)
  }

  def linkPodcastEpisode(podcastId: String, episodeId: String): Future[Unit] = Future {
    val script =
      s"""MATCH (p:$podcastLabel),(e:$episodeLabel)
         |WHERE p.id = '$podcastId' AND e.id = '$episodeId'
         |CREATE (p)-[r:PUBLISHED]->(e)
       """.stripMargin
    runScript(script)
  }

  def linkPodcastWebsite(podcastId: String, url: String): Future[Unit] = Future {
    val script =
      s"""MATCH (p:$podcastLabel),(w:$websiteLabel)
         |WHERE p.id = '$podcastId' AND w.url = '$url'
         |CREATE (p)-[r:REFERENCES]->(w)
       """.stripMargin
    runScript(script)
  }

  def linkEpisodeWebsite(episodeId: String, url: String): Future[Unit] = Future {
    val script =
      s"""MATCH (e:$episodeLabel),(w:$websiteLabel)
         |WHERE e.id = '$episodeId' AND w.url = '$url'
         |CREATE (e)-[r:REFERENCES]->(w)
       """.stripMargin
    runScript(script)
  }

}
