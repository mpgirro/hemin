package hemin.engine.semantic

import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.scalalogging.Logger
import hemin.engine.semantic.SemanticStore._
import hemin.engine.semantic.repository.graph.{GraphRepository, Neo4jRepository}
import hemin.engine.model.{Episode, Podcast}
import hemin.engine.node.Node.{ActorRefSupervisor, ReportSemanticStoreInitializationComplete}

import scala.concurrent.ExecutionContext

object SemanticStore {
  final val name = "semantic"

  def props(config: SemanticConfig): Props =
    Props(new SemanticStore(config))
      .withDispatcher(config.dispatcher)
      .withMailbox(config.mailbox)

  trait GraphStoreMessage
  final case class GeneratePodcastNode(podcast: Podcast) extends GraphStoreMessage
  final case class GenerateEpisodetNode(episode: Episode) extends GraphStoreMessage
  final case class GeneratePodcastEpisodeRelationship(podcastId: Option[String], episodeId: Option[String]) extends GraphStoreMessage
}

class SemanticStore(config: SemanticConfig)
  extends Actor {

  private val log: Logger = Logger(getClass)

  log.debug("{} running on dispatcher : {}", self.path.name, context.system.dispatchers.lookup(context.props.dispatcher))
  log.debug("{} running with mailbox : {}", self.path.name, context.system.mailboxes.lookup(context.props.mailbox))

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private val graphRepository: GraphRepository = new Neo4jRepository(config, executionContext)

  private var supervisor: ActorRef = _

  // wipe all data if it pleases and sparkles
  if (config.createStore) {
    log.info("Deleting Semantic database(s) on startup")
    graphRepository.dropAll()
  }

  override def postStop: Unit = {
    log.info("{} subsystem shutting down", SemanticStore.name.toUpperCase)
  }

  override def receive: Receive = {

    case ActorRefSupervisor(ref) =>
      log.debug("Received ActorRefSupervisor(_)")
      supervisor = ref
      supervisor ! ReportSemanticStoreInitializationComplete

    case GeneratePodcastNode(podcast) =>
      onGeneratePodcastNode(podcast)

    case GenerateEpisodetNode(episode) =>
      onGenerateEpisodetNode(episode)

    case GeneratePodcastEpisodeRelationship(podcastId, episodeId) =>
      onGeneratePodcastEpisodeRelationship(podcastId, episodeId)

  }

  override def unhandled(msg: Any): Unit = {
    super.unhandled(msg)
    log.error("Received unhandled message of type : {}", msg.getClass)
  }

  private def onGeneratePodcastNode(podcast: Podcast): Unit = {
    log.debug("Received GeneratePodcastNode({})", podcast.id)
    graphRepository.createPodcast(podcast)
    podcast.id.foreach { podcastId =>
      podcast.link.foreach { websiteUrl =>
        graphRepository.createWebsite(websiteUrl)
        graphRepository.linkPodcastWebsite(podcastId, websiteUrl)
      }
      podcast.persona.authors.foreach { person =>
        graphRepository.createPerson(person)
        graphRepository.linkPodcastPerson(podcastId, person, GraphRepository.AUTHOR_RELATIONSHIP)
      }
      podcast.persona.contributors.foreach { person =>
        graphRepository.createPerson(person)
        graphRepository.linkPodcastPerson(podcastId, person, GraphRepository.CONTRIBUTOR_RELATIONSHIP)
      }
      podcast.itunes.owner.foreach { person =>
        graphRepository.createPerson(person)
        graphRepository.linkPodcastPerson(podcastId, person, GraphRepository.ITUNES_OWNER_RELATIONSHIP)
      }
    }
  }

  private def onGenerateEpisodetNode(episode: Episode): Unit = {
    log.debug("Received GenerateEpisodetNode({})", episode.id)
    graphRepository.createEpisode(episode)
    episode.id.foreach { episodeId =>
      episode.link.foreach { websiteUrl =>
        graphRepository.createWebsite(websiteUrl)
        graphRepository.linkEpisodeWebsite(episodeId, websiteUrl)
      }
      episode.persona.authors.foreach { person =>
        graphRepository.createPerson(person)
        graphRepository.linkEpisodePerson(episodeId, person, GraphRepository.AUTHOR_RELATIONSHIP)
      }
      episode.persona.contributors.foreach { person =>
        graphRepository.createPerson(person)
        graphRepository.linkEpisodePerson(episodeId, person, GraphRepository.CONTRIBUTOR_RELATIONSHIP)
      }
      episode.itunes.author.foreach { itunesAuthor =>
        graphRepository.createPerson(itunesAuthor)
        graphRepository.linkEpisodePerson(episodeId, itunesAuthor, GraphRepository.ITUNES_AUTHOR_RELATIONSHIP)
      }
    }
  }

  private def onGeneratePodcastEpisodeRelationship(podcastId: Option[String], episodeId: Option[String]): Unit = {
    log.debug("Received GeneratePodcastEpisodeRelationship({},{})", podcastId, episodeId)
    (podcastId, episodeId) match {
      case (Some(pId), Some(eId)) => graphRepository.linkPodcastEpisode(pId, eId)
      case (_, _) => log.warn("Cannot create Podcast-Episode relationship for : ({},{})", podcastId, episodeId)
    }
  }

}
