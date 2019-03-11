package hemin.engine.graph

import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.scalalogging.Logger
import hemin.engine.graph.GraphStore._
import hemin.engine.graph.repository.{GraphRepository, Neo4jRepository}
import hemin.engine.model.{Episode, Podcast}
import hemin.engine.node.Node.{ActorRefSupervisor, ReportGraphStoreInitializationComplete}

import scala.concurrent.ExecutionContext

object GraphStore {
  final val name = "graph"

  def props(config: GraphConfig): Props =
    Props(new GraphStore(config))
      .withDispatcher(config.dispatcher)
      .withMailbox(config.mailbox)

  trait GraphStoreMessage
  final case class GeneratePodcastNode(podcast: Podcast) extends GraphStoreMessage
  final case class GenerateEpisodetNode(episode: Episode) extends GraphStoreMessage
  final case class GenerateWebsiteNode(url: Option[String]) extends GraphStoreMessage
  final case class GeneratePodcastEpisodeRelationship(podcastId: Option[String], episodeId: Option[String]) extends GraphStoreMessage
  final case class GeneratePodcastWebsiteRelationship(podcastId: Option[String], websiteUrl: Option[String]) extends GraphStoreMessage
  final case class GenerateEpisodeWebsiteRelationship(episodeId: Option[String], websiteUrl: Option[String]) extends GraphStoreMessage
}

class GraphStore (config: GraphConfig)
  extends Actor {

  private val log: Logger = Logger(getClass)

  log.debug("{} running on dispatcher : {}", self.path.name, context.system.dispatchers.lookup(context.props.dispatcher))
  log.debug("{} running with mailbox : {}", self.path.name, context.system.mailboxes.lookup(context.props.mailbox))

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private val repository: GraphRepository = new Neo4jRepository(config, executionContext)

  private var supervisor: ActorRef = _

  // wipe all data if it pleases and sparkles
  if (config.createGraph) {
    log.info("Dropping graph database on startup")
    repository.dropAll()
  }

  override def postStop: Unit = {
    log.info("{} subsystem shutting down", GraphStore.name.toUpperCase)
  }

  override def receive: Receive = {

    case ActorRefSupervisor(ref) =>
      log.debug("Received ActorRefSupervisor(_)")
      supervisor = ref
      supervisor ! ReportGraphStoreInitializationComplete

    case GeneratePodcastNode(podcast) =>
      onGeneratePodcastNode(podcast)

    case GenerateEpisodetNode(episode) =>
      onGenerateEpisodetNode(episode)

    case GenerateWebsiteNode(url) =>
      onGenerateWebsiteNode(url)

    case GeneratePodcastEpisodeRelationship(podcastId, episodeId) =>
      onGeneratePodcastEpisodeRelationship(podcastId, episodeId)

    case GeneratePodcastWebsiteRelationship(podcastId, websiteUrl) =>
      onGeneratePodcastWebsiteRelationship(podcastId, websiteUrl)

    case GenerateEpisodeWebsiteRelationship(episodeId, websiteUrl) =>
      onGenerateEpisodeWebsiteRelationship(episodeId, websiteUrl)

  }

  override def unhandled(msg: Any): Unit = {
    super.unhandled(msg)
    log.error("Received unhandled message of type : {}", msg.getClass)
  }

  private def onGeneratePodcastNode(podcast: Podcast): Unit = {
    log.debug("Received GeneratePodcastNode({})", podcast.id)
    repository.createPodcast(podcast)
  }

  private def onGenerateEpisodetNode(episode: Episode): Unit = {
    log.debug("Received GenerateEpisodetNode({})", episode.id)
    repository.createEpisode(episode)
  }

  private def onGenerateWebsiteNode(websiteUrl: Option[String]): Unit = {
    log.debug("Received onGenerateWebsiteNode('{}')", websiteUrl)
    websiteUrl match {
      case Some(url) => repository.createWebsite(url)
      case None      => log.warn("Cannot create Website node for empty URL")
    }
  }

  private def onGeneratePodcastEpisodeRelationship(podcastId: Option[String], episodeId: Option[String]): Unit = {
    log.debug("Received GeneratePodcastEpisodeRelationship({},{})", podcastId, episodeId)
    (podcastId, episodeId) match {
      case (Some(pId), Some(eId)) => repository.linkPodcastEpisode(pId, eId)
      case (_, _) => log.warn("Cannot create Podcast-Episode relationship for : ({},{})", podcastId, episodeId)
    }
  }

  private def onGeneratePodcastWebsiteRelationship(podcastId: Option[String], websiteUrl: Option[String]): Unit = {
    log.debug("Received GeneratePodcastWebsiteRelationship({},'{}')", podcastId, websiteUrl)
    (podcastId, websiteUrl) match {
      case (Some(pId), Some(url)) => repository.linkPodcastWebsite(pId, url)
      case (_, _) => log.warn("Cannot create Podcast-Website relationship for : ({},{})", podcastId, websiteUrl)
    }
  }

  private def onGenerateEpisodeWebsiteRelationship(episodeId: Option[String], websiteUrl: Option[String]): Unit = {
    log.debug("Received GenerateEpisodeWebsiteRelationship({},'{}')", episodeId, websiteUrl)
    (episodeId, websiteUrl) match {
      case (Some(eId), Some(url)) => repository.linkEpisodeWebsite(eId, url)
      case (_, _) => log.warn("Cannot create Episode-Website relationship for : ({},{})", episodeId, websiteUrl)
    }

  }

}
