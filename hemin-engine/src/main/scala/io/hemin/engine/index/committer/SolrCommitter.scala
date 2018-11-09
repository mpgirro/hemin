package io.hemin.engine.index.committer

import java.io.IOException
import java.util.concurrent.ExecutorService

import com.typesafe.scalalogging.Logger
import io.hemin.engine.index.IndexConfig
import io.hemin.engine.model.IndexDoc
import io.hemin.engine.util.mapper.SolrMapper
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient
import org.apache.solr.client.solrj.{SolrClient, SolrServerException}

import scala.util.{Failure, Success}


class SolrCommitter(config: IndexConfig,
                    executorService: ExecutorService)
  extends IndexCommitter {

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
      log.error("Error deleting all old documents from Solr collection")
      ex.printStackTrace()
  }

  override def save(doc: IndexDoc): Unit = {
    SolrMapper.toSolr(doc) match {
      case Success(d) =>
        solr.add(d)
        solr.commit()
      case Failure(ex) =>
        log.error("Failed to map IndexDoc to Solr; reason : {}", ex.getMessage)
        ex.printStackTrace()
    }
  }
}
