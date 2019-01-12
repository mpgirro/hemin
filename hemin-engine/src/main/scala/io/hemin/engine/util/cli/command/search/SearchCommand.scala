package io.hemin.engine.util.cli.command.search

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import io.hemin.engine.searcher.{Searcher, SearcherConfig}
import io.hemin.engine.util.cli.CliFormatter
import io.hemin.engine.util.cli.command.CliCommand

import scala.concurrent.{ExecutionContext, Future}

class SearchCommand (bus: ActorRef,
                     config: SearcherConfig)
                    (override implicit val executionContext: ExecutionContext,
                     override implicit val internalTimeout: Timeout)
  extends CliCommand {

  override lazy val usageDefs: List[String] = List(
    "search QUERY [QUERY [...]]",
  )

  override def eval(cmd: List[String]): Future[String] = cmd match {
    case words: List[String] => getSearchResult(words)
    case _                   => usageResult
  }

  private def getSearchResult(words: List[String]): Future[String] = {
    val query: String = words.mkString(" ")
    val pageNumber: Option[Int] = Some(config.defaultPage)
    val pageSize: Option[Int] = Some(config.defaultSize)
    CliFormatter.cliResult(bus ? Searcher.SearchRequest(query, pageNumber, pageSize))
  }
}
