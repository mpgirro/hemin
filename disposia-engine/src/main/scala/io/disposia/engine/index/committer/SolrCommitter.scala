package io.disposia.engine.index.committer

import java.io.IOException
import java.util.concurrent.ExecutorService

import com.typesafe.scalalogging.Logger
import io.disposia.engine.index.IndexConfig
import io.disposia.engine.domain.IndexDoc
import io.disposia.engine.util.mapper.SolrMapper.toSolr
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient
import org.apache.solr.client.solrj.{SolrClient, SolrServerException}


class SolrCommitter(config: IndexConfig, executorService: ExecutorService) extends IndexCommitter {

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

  override def save(doc: IndexDoc): Unit = {

    // TODO do not always solr.add (produces duplicates), but update by EXO instead

    solr.add(toSolr(doc))
    solr.commit()
  }
}
