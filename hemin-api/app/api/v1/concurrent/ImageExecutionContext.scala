package api.v1.concurrent

import akka.actor.ActorSystem
import javax.inject.Inject
import play.api.libs.concurrent.CustomExecutionContext

class ImageExecutionContext @Inject()(actorSystem: ActorSystem) extends CustomExecutionContext(actorSystem, "image.dispatcher")