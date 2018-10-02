package io.disposia.engine.index

import java.io.IOException
import java.util.concurrent.ExecutorService

import com.typesafe.scalalogging.Logger
import io.disposia.engine.domain.{Episode, IndexDoc, Podcast}
import io.disposia.engine.mapper.IndexMapper
import io.disposia.engine.util.mapper.SolrMapper.toSolr
import io.disposia.engine.util.mapper.SolrMapper
import org.apache.solr.client.solrj.{SolrClient, SolrServerException}
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient
import org.apache.solr.common.SolrInputDocument

import scala.concurrent.ExecutionContextExecutorService


class SolrCommitter(config: IndexConfig, executorService: ExecutorService) {

  private val log: Logger = Logger(getClass)

  private val solr: SolrClient = new ConcurrentUpdateSolrClient.Builder(config.solrUri)
    .withQueueSize(config.solrQueueSize)
    .withThreadCount(config.solrThreadCount)
    .withExecutorService(executorService)
    .build()

  if (config.createIndex) try {
    log.info("Deleting all Solr documents on startup")
    solr.deleteByQuery("*:*")
    solr.commit()
  } catch {
    case ex@(_: SolrServerException | _: IOException) =>
      log.error("Error deleting all old document from Solr collection")
      ex.printStackTrace()
  }

  def add(doc: IndexDoc): Unit = {
    solr.add(toSolr(doc))
    solr.commit() // TODO are the commits really buffered transparently?
  }

}
