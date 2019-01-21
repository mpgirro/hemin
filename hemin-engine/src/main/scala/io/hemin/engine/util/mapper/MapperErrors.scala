package io.hemin.engine.util.mapper

import io.hemin.engine.HeminException
import io.hemin.engine.model.{Episode, IndexDoc, Podcast}

import scala.util.{Failure, Try}

object MapperErrors {

  /** Returns the result of [[MapperErrors.mapperErrorIndexToPodcast]] as a Failure */
  private[mapper] def mapperFailureIndexToPodcast(value: IndexDoc): Try[Podcast] =
    Failure(mapperErrorIndexToPodcast(value))
  private[mapper] def mapperErrorIndexToPodcast(value: IndexDoc): HeminException =
    new HeminException(s"Error mapping IndexDoc to Podcast : $value")

  /** Returns the result of [[MapperErrors.mapperErrorLuceneToPodcast]] as a Failure */
  private[mapper] def mapperFailureLuceneToPodcast(value: org.apache.lucene.document.Document): Try[Podcast] =
    Failure(mapperErrorLuceneToPodcast(value))
  private[mapper] def mapperErrorLuceneToPodcast(value: org.apache.lucene.document.Document): HeminException =
    new HeminException(s"Error mapping Lucene Document to Podcast : $value")

  /** Returns the result of [[MapperErrors.mapperErrorSolrToPodcast]] as a Failure */
  private[mapper] def mapperFailureSolrToPodcast(value: org.apache.solr.common.SolrDocument): Try[Podcast] =
    Failure(mapperErrorSolrToPodcast(value))
  private[mapper] def mapperErrorSolrToPodcast(value: org.apache.solr.common.SolrDocument): HeminException =
    new HeminException(s"Error mapping Solr Document to Podcast : $value")

  /** Returns the result of [[MapperErrors.mapperErrorLuceneToIndexDoc]] as a Failure */
  private[mapper] def mapperFailureLuceneToIndexDoc(value: org.apache.lucene.document.Document): Try[IndexDoc] =
    Failure(mapperErrorLuceneToIndexDoc(value))
  private[mapper] def mapperErrorLuceneToIndexDoc(value: org.apache.lucene.document.Document): HeminException =
    new HeminException(s"Error mapping Lucene Document to IndexDoc : $value")

  /** Returns the result of [[MapperErrors.mapperErrorSolrToIndexDoc]] as a Failure */
  private[mapper] def mapperFailureSolrToIndexDoc(value: org.apache.solr.common.SolrDocument): Try[IndexDoc] =
    Failure(mapperErrorSolrToIndexDoc(value))
  private[mapper] def mapperErrorSolrToIndexDoc(value: org.apache.solr.common.SolrDocument): HeminException =
    new HeminException(s"Error mapping Solr to IndexDoc : $value")

  /** Returns the result of [[MapperErrors.mapperErrorPodcastToIndexDoc]] as a Failure */
  private[mapper] def mapperFailurePodcastToIndexDoc(value: Podcast): Try[IndexDoc] = Failure(mapperErrorPodcastToIndexDoc(value))
  private[mapper] def mapperFailurePodcastToIndexDoc(cause: Throwable): Try[IndexDoc] = Failure(cause)
  private[mapper] def mapperErrorPodcastToIndexDoc(value: Podcast): HeminException =
    new HeminException(s"Error mapping Podcast to IndexDoc : $value")

  /** Returns the result of [[MapperErrors.mapperErrorEpisodeToIndexDoc]] as a Failure */
  private[mapper] def mapperFailureEpisodeToIndexDoc(value: Episode): Try[IndexDoc] = Failure(mapperErrorEpisodeToIndexDoc(value))
  private[mapper] def mapperFailureEpisodeToIndexDoc(cause: Throwable): Try[IndexDoc] = Failure(cause)
  private[mapper] def mapperErrorEpisodeToIndexDoc(value: Episode): HeminException =
    new HeminException(s"Error mapping Episode to IndexDoc : $value")

  /** Returns the result of [[MapperErrors.mapperErrorUnsupportedIndexDocumentType]] as a Failure */
  private[mapper] def mapperFailureUnsupportedIndexDocumentType(value: String): Try[IndexDoc] =
    Failure(mapperErrorUnsupportedIndexDocumentType(value))
  private[mapper] def mapperErrorUnsupportedIndexDocumentType(value: String): HeminException =
    new HeminException("Unsupported document type : " + value)

  /** Returns the result of [[MapperErrors.mapperErrorIndexFieldNotPresent]] as a Failure */
  private[mapper] def mapperFailureIndexFieldNotPresent(value: String): Try[IndexDoc] = Failure(mapperErrorIndexFieldNotPresent(value))
  private[mapper] def mapperErrorIndexFieldNotPresent(value: String): HeminException =
    new HeminException(s"Field '$value' could not be extracted")

  /** Returns the result of [[MapperErrors.mapperErrorIndexToEpisode]] as a Failure */
  private[mapper] def mapperFailureIndexToEpisode(value: IndexDoc): Try[Episode] = Failure(mapperErrorIndexToEpisode(value))
  private[mapper] def mapperErrorIndexToEpisode(value: IndexDoc): HeminException =
    new HeminException(s"Error mapping IndexDoc to Episode : $value")

  /** Returns the result of [[MapperErrors.mapperErrorLuceneToEpisode]] as a Failure */
  private[mapper] def mapperFailureLuceneToEpisode(value: org.apache.lucene.document.Document): Try[Episode] =
    Failure(mapperErrorLuceneToEpisode(value))
  private[mapper] def mapperErrorLuceneToEpisode(value: org.apache.lucene.document.Document): HeminException =
    new HeminException(s"Error mapping Lucene Document to Episode : $value")

  /** Returns the result of [[MapperErrors.mapperErrorSolrToEpisode]] as a Failure */
  private[mapper] def mapperFailureSolrToEpisode(value: org.apache.solr.common.SolrDocument): Try[Episode] =
    Failure(mapperErrorSolrToEpisode(value))
  private[mapper] def mapperErrorSolrToEpisode(value: org.apache.solr.common.SolrDocument): HeminException =
    new HeminException(s"Error mapping Solr Document to Episode : $value")

  /** Returns the result of [[MapperErrors.mapperErrorIndexToLucene]] as a Failure */
  private[mapper] def mapperFailureIndexToLucene(value: IndexDoc): Try[org.apache.lucene.document.Document] =
    Failure(mapperErrorIndexToLucene(value))
  private[mapper] def mapperErrorIndexToLucene(value: IndexDoc): HeminException =
    new HeminException(s"Error mapping IndexDoc to Lucene Document : $value")

  /** Returns the result of [[MapperErrors.mapperErrorIndexToSolr]] as a Failure */
  private[mapper] def mapperFailureIndexToSolr(value: IndexDoc): Try[org.apache.solr.common.SolrInputDocument] =
    Failure(mapperErrorIndexToSolr(value))
  private[mapper] def mapperErrorIndexToSolr(value: IndexDoc): HeminException =
    new HeminException(s"Error mapping IndexDoc to Solr Document : $value")

}
