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

  override def createPodcast(podcast: Podcast): Future[Unit] = Future {
    val script = (podcast.id, podcast.title) match {
      case (Some(id), Some(title)) =>
        s"CREATE (podcast:$podcastLabel {title:'$title',id:'$id'})"
      case (Some(id), _) =>
        s"CREATE (podcast:$podcastLabel {id:'$id'})"
      case (_,_) =>
        log.warn("Unable to create Podcast node since we have neither ID nor title")
        ""
    }
    //val script = s"CREATE (podcast:Podcasts {title:'${podcast.title}',id:'${podcast.id}'})"
    /*
    val script =
      s"""MERGE (p:Podcast{ id: { map }.id, title: { map }.title }
         |ON CREATE SET n = { map }
         |ON MATCH  SET n += { map }
       """.stripMargin
       */
    runScript(script)
  }

  override def createEpisode(episode: Episode): Future[Unit] = Future {
    val script = (episode.id, episode.title) match {
      case (Some(id), Some(title)) =>
        s"CREATE (episode:$episodeLabel {title:'$title',id:'$id'})"
      case (Some(id), _) =>
        s"CREATE (episode:$episodeLabel {id:'$id'})"
      case (_,_) =>
        log.warn("Unable to create Episode node since we have neither ID nor title")
        ""
    }
    //val script = s"CREATE (episode:Episodes {title:'${episode.title}',id:'${episode.id}'})"
    /*
    val script =
      s"""MERGE (e:Episode{ id: { map }.id, title: { map }.title }
         |ON CREATE SET n = { map }
         |ON MATCH  SET n += { map }
       """.stripMargin
       */
    runScript(script)
  }

  override def createWebsite(url: String): Future[Unit] = Future {
    val script = s"CREATE (website:$websiteLabel {url:'$url'})"
    /*
    val script =
      s"""MERGE (w:Website{ url: { map }.url }
         |ON CREATE SET n = { map }
         |ON MATCH  SET n += { map }
       """.stripMargin
       */
    runScript(script)
  }

  override def createPerson(person: Person): Future[Unit] = Future {
    val script = (person.name, person.email, person.uri) match {
      case (Some(name), Some(email), Some(uri)) =>
        s"CREATE (person:$personLabel {name:'$name',email:'$email',uri:'$uri'})"
      case (Some(name), None, Some(uri)) =>
        s"CREATE (person:$personLabel {name:'$name',uri:'$uri'})"
      case (Some(name), Some(email), None) =>
        s"CREATE (person:$personLabel {name:'$name',email:'$email'})"
      case (None, Some(email), Some(uri)) =>
        s"CREATE (person:$personLabel email:'$email',uri:'$uri'})"
      case (Some(name), _, _) =>
        s"CREATE (person:$personLabel {name:'$name')"
      case (_,_,_) =>
        log.warn("Unable to create Person node since we have neither name, email nor uri")
        ""
    }
    //val script = s"CREATE (person:Persons {name:'${person.name}',email:'${person.email}', uri:'${person.uri}'})"
    /*
    val script =
      s"""MERGE (p:Person{ name: { map }.name, email: { map }.email, uri: { map }.uri }
         |ON CREATE SET n = { map }
         |ON MATCH  SET n += { map }
       """.stripMargin
       */
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
