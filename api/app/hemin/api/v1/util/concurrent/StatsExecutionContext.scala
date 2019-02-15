package hemin.api.v1.util.concurrent

import akka.actor.ActorSystem
import javax.inject.Inject
import play.api.libs.concurrent.CustomExecutionContext

class StatsExecutionContext @Inject()(actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "hemin.api.v1.stats.dispatcher")
