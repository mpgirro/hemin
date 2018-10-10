package io.hemin.engine.index

import com.typesafe.scalalogging.Logger
import io.hemin.engine.domain.ResultsWrapper
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.impl.HttpSolrClient

import scala.concurrent.{ExecutionContext, Future}


class SolrSearcher (solrUri: String)(implicit executionContext: ExecutionContext) {

  private val log: Logger = Logger(getClass)

  private val solr: SolrClient = new HttpSolrClient.Builder(solrUri)
    .allowCompression(false) // TODO
    .build()

  def search(query: String): Future[ResultsWrapper] = Future {
    // TODO
    ResultsWrapper()
  }

}
