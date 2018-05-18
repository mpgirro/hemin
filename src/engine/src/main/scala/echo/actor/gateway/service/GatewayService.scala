package echo.actor.gateway.service

import akka.dispatch.MessageDispatcher
import akka.http.scaladsl.server.{Directives, Route}
import echo.actor.gateway.json.JsonSupport

/**
  * @author Maximilian Irro
  */
trait GatewayService {

    val DISPATCHER_ID = "echo.gateway.dispatcher"
    implicit val blockingDispatcher: MessageDispatcher
    val route: Route

}
