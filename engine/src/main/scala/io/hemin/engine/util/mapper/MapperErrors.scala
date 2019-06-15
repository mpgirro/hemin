package io.hemin.engine.util.mapper

import io.hemin.engine.HeminException
import io.hemin.engine.model.{Document, Episode, Person, Podcast}

import scala.util.{Failure, Try}

object MapperErrors {

  /** Returns the result of [[MapperErrors.mapperErrorIndexToPodcast]] as a Failure */
  private[mapper] def mapperFailureIndexToPodcast(value: Document): Try[Podcast] =
    Failure(mapperErrorIndexToPodcast(value))
  private[mapper] def mapperErrorIndexToPodcast(value: Document): HeminException =
    new HeminException(s"Error mapping IndexDoc to Podcast : $value")

  /** Returns the result of [[MapperErrors.mapperErrorSolrToPodcast]] as a Failure */
  private[mapper] def mapperFailureSolrToPodcast(value: org.apache.solr.common.SolrDocument): Try[Podcast] =
    Failure(mapperErrorSolrToPodcast(value))
  private[mapper] def mapperErrorSolrToPodcast(value: org.apache.solr.common.SolrDocument): HeminException =
    new HeminException(s"Error mapping Solr Document to Podcast : $value")

  /** Returns the result of [[MapperErrors.mapperErrorSolrToIndexDoc]] as a Failure */
  private[mapper] def mapperFailureSolrToIndexDoc(value: org.apache.solr.common.SolrDocument): Try[Document] =
    Failure(mapperErrorSolrToIndexDoc(value))
  private[mapper] def mapperErrorSolrToIndexDoc(value: org.apache.solr.common.SolrDocument): HeminException =
    new HeminException(s"Error mapping Solr to IndexDoc : $value")

  /** Returns the result of [[MapperErrors.mapperErrorPodcastToIndexDoc]] as a Failure */
  private[mapper] def mapperFailurePodcastToIndexDoc(value: Podcast): Try[Document] = Failure(mapperErrorPodcastToIndexDoc(value))
  private[mapper] def mapperFailurePodcastToIndexDoc(cause: Throwable): Try[Document] = Failure(cause)
  private[mapper] def mapperErrorPodcastToIndexDoc(value: Podcast): HeminException =
    new HeminException(s"Error mapping Podcast to IndexDoc : $value")

  /** Returns the result of [[MapperErrors.mapperErrorEpisodeToIndexDoc]] as a Failure */
  private[mapper] def mapperFailureEpisodeToIndexDoc(value: Episode): Try[Document] = Failure(mapperErrorEpisodeToIndexDoc(value))
  private[mapper] def mapperFailureEpisodeToIndexDoc(cause: Throwable): Try[Document] = Failure(cause)
  private[mapper] def mapperErrorEpisodeToIndexDoc(value: Episode): HeminException =
    new HeminException(s"Error mapping Episode to IndexDoc : $value")

  /** Returns the result of [[MapperErrors.mapperErrorPersonToIndexDoc]] as a Failure */
  private[mapper] def mapperFailurePersonToIndexDoc(value: Person): Try[Document] = Failure(mapperErrorPersonToIndexDoc(value))
  private[mapper] def mapperFailurePersonToIndexDoc(cause: Throwable): Try[Document] = Failure(cause)
  private[mapper] def mapperErrorPersonToIndexDoc(value: Person): HeminException =
    new HeminException(s"Error mapping Person to IndexDoc : $value")

  /** Returns the result of [[MapperErrors.mapperErrorUnsupportedIndexDocumentType]] as a Failure */
  private[mapper] def mapperFailureUnsupportedIndexDocumentType(value: String): Try[Document] =
    Failure(mapperErrorUnsupportedIndexDocumentType(value))
  private[mapper] def mapperErrorUnsupportedIndexDocumentType(value: String): HeminException =
    new HeminException("Unsupported document type : " + value)

  /** Returns the result of [[MapperErrors.mapperErrorIndexFieldNotPresent]] as a Failure */
  private[mapper] def mapperFailureIndexFieldNotPresent(value: String): Try[Document] = Failure(mapperErrorIndexFieldNotPresent(value))
  private[mapper] def mapperErrorIndexFieldNotPresent(value: String): HeminException =
    new HeminException(s"Field '$value' could not be extracted")

  /** Returns the result of [[MapperErrors.mapperErrorIndexToEpisode]] as a Failure */
  private[mapper] def mapperFailureIndexToEpisode(value: Document): Try[Episode] = Failure(mapperErrorIndexToEpisode(value))
  private[mapper] def mapperErrorIndexToEpisode(value: Document): HeminException =
    new HeminException(s"Error mapping IndexDoc to Episode : $value")

  /** Returns the result of [[MapperErrors.mapperErrorSolrToEpisode]] as a Failure */
  private[mapper] def mapperFailureSolrToEpisode(value: org.apache.solr.common.SolrDocument): Try[Episode] =
    Failure(mapperErrorSolrToEpisode(value))
  private[mapper] def mapperErrorSolrToEpisode(value: org.apache.solr.common.SolrDocument): HeminException =
    new HeminException(s"Error mapping Solr Document to Episode : $value")

  /** Returns the result of [[MapperErrors.mapperErrorIndexToSolr]] as a Failure */
  private[mapper] def mapperFailureIndexToSolr(value: Document): Try[org.apache.solr.common.SolrInputDocument] =
    Failure(mapperErrorIndexToSolr(value))
  private[mapper] def mapperErrorIndexToSolr(value: Document): HeminException =
    new HeminException(s"Error mapping IndexDoc to Solr Document : $value")

}
