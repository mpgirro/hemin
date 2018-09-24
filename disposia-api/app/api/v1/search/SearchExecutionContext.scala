package api.v1.search

import akka.actor.ActorSystem
import javax.inject.Inject
import play.api.libs.concurrent.CustomExecutionContext

class SearchExecutionContext @Inject()(actorSystem: ActorSystem) extends CustomExecutionContext(actorSystem, "search.dispatcher")
