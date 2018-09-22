package v1.podcast

import akka.actor.ActorSystem
import javax.inject.Inject
import play.api.libs.concurrent.CustomExecutionContext

class PodcastExecutionContext @Inject()(actorSystem: ActorSystem) extends CustomExecutionContext(actorSystem, "podcast.dispatcher")
