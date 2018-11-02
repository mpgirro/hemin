package io.hemin.engine.util

import io.hemin.engine.{Engine, EngineException}
import io.hemin.engine.model.{Episode, IndexDoc, Podcast}

import scala.util.{Failure, Try}

object Errors {

  /** Returns the result of [[Errors.engineStartupError]] as a Failure */
  def engineStartupFailure(ex: Throwable): Try[Engine] = Failure(engineStartupError(ex))
  def engineStartupError(ex: Throwable): EngineException =
    new EngineException(s"Engine startup failed; reason : ${ex.getMessage}", ex)

  lazy val engineGuardErrorNotRunning: EngineException =
    new EngineException("Guard prevented call; reason: Engine not running")
  def engineGuardFailureNotRunning[A]: Try[A] = Failure(engineGuardErrorNotRunning)

  lazy val engineShutdownErrorNotRunning: EngineException =
    new EngineException("Engine shutdown failed; reason: not running")
  lazy val engineShutdownFailureNotRunning: Try[Unit] = Failure(engineShutdownErrorNotRunning)

  /** Returns the result of [[Errors.engineShutdownFailure]] as a Failure */
  def engineShutdownFailure(ex: Throwable): Try[Unit] = Failure(engineShutdownError(ex))
  def engineShutdownError(ex: Throwable): EngineException =
    new EngineException(s"Engine shutdown failed; reason : ${ex.getMessage}", ex)

  /** Returns the result of [[Errors.mapperErrorIndexToPodcast]] as a Failure */
  def mapperFailureIndexToPodcast(value: IndexDoc): Try[Podcast] = Failure(mapperErrorIndexToPodcast(value))
  def mapperErrorIndexToPodcast(value: IndexDoc): EngineException =
    new EngineException(s"Error mapping IndexDoc to Podcast : $value")

  /** Returns the result of [[Errors.mapperErrorLuceneToPodcast]] as a Failure */
  def mapperFailureLuceneToPodcast(value: org.apache.lucene.document.Document): Try[Podcast] =
    Failure(mapperErrorLuceneToPodcast(value))
  def mapperErrorLuceneToPodcast(value: org.apache.lucene.document.Document): EngineException =
    new EngineException(s"Error mapping Lucene Document to Podcast : $value")

  /** Returns the result of [[Errors.mapperErrorSolrToPodcast]] as a Failure */
  def mapperFailureSolrToPodcast(value: org.apache.solr.common.SolrDocument): Try[Podcast] =
    Failure(mapperErrorSolrToPodcast(value))
  def mapperErrorSolrToPodcast(value: org.apache.solr.common.SolrDocument): EngineException =
    new EngineException(s"Error mapping Solr Document to Podcast : $value")

  /** Returns the result of [[Errors.mapperErrorLuceneToIndexDoc]] as a Failure */
  def mapperFailureLuceneToIndexDoc(value: org.apache.lucene.document.Document): Try[IndexDoc] =
    Failure(mapperErrorLuceneToIndexDoc(value))
  def mapperErrorLuceneToIndexDoc(value: org.apache.lucene.document.Document): EngineException =
    new EngineException(s"Error mapping Lucene Document to IndexDoc : $value")

  /** Returns the result of [[Errors.mapperErrorSolrToIndexDoc]] as a Failure */
  def mapperFailureSolrToIndexDoc(value: org.apache.solr.common.SolrDocument): Try[IndexDoc] =
    Failure(mapperErrorSolrToIndexDoc(value))
  def mapperErrorSolrToIndexDoc(value: org.apache.solr.common.SolrDocument): EngineException =
    new EngineException(s"Error mapping Solr to IndexDoc : $value")

  /** Returns the result of [[Errors.mapperErrorPodcastToIndexDoc]] as a Failure */
  def mapperFailurePodcastToIndexDoc(value: Podcast): Try[IndexDoc] = Failure(mapperErrorPodcastToIndexDoc(value))
  def mapperFailurePodcastToIndexDoc(cause: Throwable): Try[IndexDoc] = Failure(cause)
  def mapperErrorPodcastToIndexDoc(value: Podcast): EngineException =
    new EngineException(s"Error mapping Podcast to IndexDoc : $value")

  /** Returns the result of [[Errors.mapperErrorEpisodeToIndexDoc]] as a Failure */
  def mapperFailureEpisodeToIndexDoc(value: Episode): Try[IndexDoc] = Failure(mapperErrorEpisodeToIndexDoc(value))
  def mapperFailureEpisodeToIndexDoc(cause: Throwable): Try[IndexDoc] = Failure(cause)
  def mapperErrorEpisodeToIndexDoc(value: Episode): EngineException =
    new EngineException(s"Error mapping Episode to IndexDoc : $value")

  /** Returns the result of [[Errors.mapperErrorUnsupportedIndexDocumentType]] as a Failure */
  def mapperFailureUnsupportedIndexDocumentType(value: String): Try[IndexDoc] =
    Failure(mapperErrorUnsupportedIndexDocumentType(value))
  def mapperErrorUnsupportedIndexDocumentType(value: String): EngineException =
    new EngineException("Unsupported document type : " + value)

  /** Returns the result of [[Errors.mapperErrorIndexFieldNotPresent]] as a Failure */
  def mapperFailureIndexFieldNotPresent(value: String): Try[IndexDoc] = Failure(mapperErrorIndexFieldNotPresent(value))
  def mapperErrorIndexFieldNotPresent(value: String): EngineException =
    new EngineException(s"Field '$value' could not be extracted")

  /** Returns the result of [[Errors.mapperErrorIndexToEpisode]] as a Failure */
  def mapperFailureIndexToEpisode(value: IndexDoc): Try[Episode] = Failure(mapperErrorIndexToEpisode(value))
  def mapperErrorIndexToEpisode(value: IndexDoc): EngineException =
    new EngineException(s"Error mapping IndexDoc to Episode : $value")

  /** Returns the result of [[Errors.mapperErrorLuceneToEpisode]] as a Failure */
  def mapperFailureLuceneToEpisode(value: org.apache.lucene.document.Document): Try[Episode] =
    Failure(mapperErrorLuceneToEpisode(value))
  def mapperErrorLuceneToEpisode(value: org.apache.lucene.document.Document): EngineException =
    new EngineException(s"Error mapping Lucene Document to Episode : $value")

  /** Returns the result of [[Errors.mapperErrorSolrToEpisode]] as a Failure */
  def mapperFailureSolrToEpisode(value: org.apache.solr.common.SolrDocument): Try[Episode] =
    Failure(mapperErrorSolrToEpisode(value))
  def mapperErrorSolrToEpisode(value: org.apache.solr.common.SolrDocument): EngineException =
    new EngineException(s"Error mapping Solr Document to Episode : $value")

  /** Returns the result of [[Errors.mapperErrorIndexToLucene]] as a Failure */
  def mapperFailureIndexToLucene(value: IndexDoc): Try[org.apache.lucene.document.Document] =
    Failure(mapperErrorIndexToLucene(value))
  def mapperErrorIndexToLucene(value: IndexDoc): EngineException =
    new EngineException(s"Error mapping IndexDoc to Lucene Document : $value")

  /** Returns the result of [[Errors.mapperErrorIndexToSolr]] as a Failure */
  def mapperFailureIndexToSolr(value: IndexDoc): Try[org.apache.solr.common.SolrInputDocument] =
    Failure(mapperErrorIndexToSolr(value))
  def mapperErrorIndexToSolr(value: IndexDoc): EngineException =
    new EngineException(s"Error mapping IndexDoc to Solr Document : $value")

}
