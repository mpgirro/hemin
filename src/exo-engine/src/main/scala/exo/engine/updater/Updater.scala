package exo.engine.updater

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import exo.engine.EngineProtocol._
import exo.engine.catalog.CatalogProtocol.ProposeNewFeed

/**
  * @author Maximilian Irro
  */

object Updater {
    final val name = "updater"
    def props(): Props = Props(new Updater()).withDispatcher("echo.updater.dispatcher")
}

class Updater extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private var catalog: ActorRef = _
    private var crawler: ActorRef = _

    override def receive: Receive = {

        case ActorRefCatalogStoreActor(ref) =>
            log.debug("Received ActorRefCatalogActor(_)")
            catalog = ref

        case ActorRefCrawlerActor(ref) =>
            log.debug("Received ActorRefCrawlerActor(_)")
            crawler = ref

        case ProposeNewFeed(url) =>
            catalog ! ProposeNewFeed(url)

        case ProcessFeed(exo, url, job: FetchJob) =>
            crawler ! DownloadWithHeadCheck(exo, url, job)
    }

}
