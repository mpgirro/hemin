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
  final case class GeneratePodcastEpisodeRelationship(podcastId: Option[String], episodeId: Option[String]) extends GraphStoreMessage
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
    log.info("Deleting Graph database on startup")
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

    case GeneratePodcastEpisodeRelationship(podcastId, episodeId) =>
      onGeneratePodcastEpisodeRelationship(podcastId, episodeId)

  }

  override def unhandled(msg: Any): Unit = {
    super.unhandled(msg)
    log.error("Received unhandled message of type : {}", msg.getClass)
  }

  private def onGeneratePodcastNode(podcast: Podcast): Unit = {
    log.debug("Received GeneratePodcastNode({})", podcast.id)
    repository.createPodcast(podcast)
    podcast.id.foreach { podcastId =>
      podcast.link.foreach { websiteUrl =>
        repository.createWebsite(websiteUrl)
        repository.linkPodcastWebsite(podcastId, websiteUrl)
      }
      podcast.persona.authors.foreach { person =>
        repository.createPerson(person)
        repository.linkPodcastPerson(podcastId, person, GraphRepository.AUTHOR_RELATIONSHIP)
      }
      podcast.persona.contributors.foreach { person =>
        repository.createPerson(person)
        repository.linkPodcastPerson(podcastId, person, GraphRepository.CONTRIBUTOR_RELATIONSHIP)
      }
      podcast.itunes.owner.foreach { person =>
        repository.createPerson(person)
        repository.linkPodcastPerson(podcastId, person, GraphRepository.ITUNES_OWNER_RELATIONSHIP)
      }
    }
  }

  private def onGenerateEpisodetNode(episode: Episode): Unit = {
    log.debug("Received GenerateEpisodetNode({})", episode.id)
    repository.createEpisode(episode)
    episode.id.foreach { episodeId =>
      episode.link.foreach { websiteUrl =>
        repository.createWebsite(websiteUrl)
        repository.linkEpisodeWebsite(episodeId, websiteUrl)
      }
      episode.persona.authors.foreach { person =>
        repository.createPerson(person)
        repository.linkEpisodePerson(episodeId, person, GraphRepository.AUTHOR_RELATIONSHIP)
      }
      episode.persona.contributors.foreach { person =>
        repository.createPerson(person)
        repository.linkEpisodePerson(episodeId, person, GraphRepository.CONTRIBUTOR_RELATIONSHIP)
      }
      episode.itunes.author.foreach { itunesAuthor =>
        repository.createPerson(itunesAuthor)
        repository.linkEpisodePerson(episodeId, itunesAuthor, GraphRepository.ITUNES_AUTHOR_RELATIONSHIP)
      }
    }
  }

  private def onGeneratePodcastEpisodeRelationship(podcastId: Option[String], episodeId: Option[String]): Unit = {
    log.debug("Received GeneratePodcastEpisodeRelationship({},{})", podcastId, episodeId)
    (podcastId, episodeId) match {
      case (Some(pId), Some(eId)) => repository.linkPodcastEpisode(pId, eId)
      case (_, _) => log.warn("Cannot create Podcast-Episode relationship for : ({},{})", podcastId, episodeId)
    }
  }

}
