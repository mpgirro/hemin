package v1.feed

import akka.actor.ActorSystem
import javax.inject.Inject
import play.api.libs.concurrent.CustomExecutionContext

class FeedExecutionContext @Inject()(actorSystem: ActorSystem) extends CustomExecutionContext(actorSystem, "feed.dispatcher")
