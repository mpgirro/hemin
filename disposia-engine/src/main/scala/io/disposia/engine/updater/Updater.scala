package io.disposia.engine.updater

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.google.common.base.Strings.isNullOrEmpty
import io.disposia.engine.EngineProtocol._
import io.disposia.engine.catalog.CatalogStore.ProposeNewFeed
import io.disposia.engine.config.UpdaterConfig
import io.disposia.engine.crawler.Crawler.{DownloadWithHeadCheck, FetchJob}
import io.disposia.engine.updater.Updater.ProcessFeed

/**
  * @author Maximilian Irro
  */

object Updater {
    final val name = "updater"
    def props(config: UpdaterConfig): Props = Props(new Updater(config)).withDispatcher("echo.updater.dispatcher")

    trait UpdaterMessage
    case class ProcessFeed(exo: String, url: String, job: FetchJob) extends UpdaterMessage
}

class Updater (config: UpdaterConfig) extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private var catalog: ActorRef = _
    private var crawler: ActorRef = _
    private var supervisor: ActorRef = _

    override def receive: Receive = {

        case ActorRefCatalogStoreActor(ref) =>
            log.debug("Received ActorRefCatalogActor(_)")
            catalog = ref

        case ActorRefCrawlerActor(ref) =>
            log.debug("Received ActorRefCrawlerActor(_)")
            crawler = ref

        case ActorRefSupervisor(ref) =>
            log.debug("Received ActorRefSupervisor(_)")
            supervisor = ref
            supervisor ! ReportUpdaterStartupComplete

        case ProposeNewFeed(url) =>
            log.debug("Received ProposeNewFeed({})", url)
            log.info("Request to propose : {}", url)
            if (!isNullOrEmpty(url))
                catalog ! ProposeNewFeed(url)

        case ProcessFeed(exo, url, job: FetchJob) =>
            log.debug("Received ProcessFeed({},{},{})", exo, url, job)
            crawler ! DownloadWithHeadCheck(exo, url, job)
    }

}
