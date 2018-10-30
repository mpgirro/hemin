package io.hemin.engine.util

import io.hemin.engine.exception.HeminException
import io.hemin.engine.model.{Episode, IndexDoc, IndexField, Podcast}
import org.apache.solr.common.SolrDocument

import scala.util.{Failure, Try}

object Errors {

  def mapperFailureIndexToPodcast(value: IndexDoc): Try[Podcast] = Failure(mapperErrorIndexToPodcast(value))
  def mapperErrorIndexToPodcast(value: IndexDoc): HeminException =
    new HeminException(s"Error mapping IndexDoc to Podcast : $value")

  def mapperFailureLuceneToPodcast(value: org.apache.lucene.document.Document): Try[Podcast] = Failure(mapperErrorLuceneToPodcast(value))
  def mapperErrorLuceneToPodcast(value: org.apache.lucene.document.Document): HeminException =
    new HeminException(s"Error mapping Lucene Document to Podcast : $value")

  def mapperFailureSolrToPodcast(value: SolrDocument): Try[Podcast] = Failure(mapperErrorSolrToPodcast(value))
  def mapperErrorSolrToPodcast(value: SolrDocument): HeminException =
    new HeminException(s"Error mapping Solr Document to Podcast : $value")

  def mapperFailureLuceneToIndexDoc(value: org.apache.lucene.document.Document): Try[IndexDoc] = Failure(mapperErrorLuceneToIndexDoc(value))
  def mapperErrorLuceneToIndexDoc(value: org.apache.lucene.document.Document): HeminException =
    new HeminException(s"Error mapping Lucene Document to IndexDoc : $value")

  def mapperFailureSolrToIndexDoc(value: SolrDocument): Try[IndexDoc] = Failure(mapperErrorSolrToIndexDoc(value))
  def mapperErrorSolrToIndexDoc(value: SolrDocument): HeminException =
    new HeminException(s"Error mapping Solr to IndexDoc : $value")

  def mapperFailurePodcastToIndexDoc(cause: Throwable): Try[IndexDoc] = Failure(cause)
  def mapperFailurePodcastToIndexDoc(value: Podcast): Try[IndexDoc] = Failure(mapperErrorPodcastToIndexDoc(value))
  def mapperErrorPodcastToIndexDoc(value: Podcast): HeminException =
    new HeminException(s"Error mapping Podcast to IndexDoc : $value")

  def mapperFailureEpisodeToIndexDoc(cause: Throwable): Try[IndexDoc] = Failure(cause)
  def mapperFailureEpisodeToIndexDoc(value: Episode): Try[IndexDoc] = Failure(mapperErrorEpisodeToIndexDoc(value))
  def mapperErrorEpisodeToIndexDoc(value: Episode): HeminException =
    new HeminException(s"Error mapping Episode to IndexDoc : $value")

  def mapperFailureUnsupportedIndexDocumentType(value: String): Try[IndexDoc] = Failure(mapperErrorUnsupportedIndexDocumentType(value))
  def mapperErrorUnsupportedIndexDocumentType(value: String): HeminException =
    new HeminException("Unsupported document type : " + value)

  def mapperFailureIndexFieldNotPresent(value: String): Try[IndexDoc] = Failure(mapperErrorIndexFieldNotPresent(value))
  def mapperErrorIndexFieldNotPresent(value: String): HeminException =
    new HeminException(s"Field '$value' could not be extracted")

  def mapperFailureIndexToEpisode(value: IndexDoc): Try[Episode] = Failure(mapperErrorIndexToEpisode(value))
  def mapperErrorIndexToEpisode(value: IndexDoc): HeminException =
    new HeminException(s"Error mapping IndexDoc to Episode : $value")

  def mapperFailureLuceneToEpisode(value: org.apache.lucene.document.Document): Try[Episode] = Failure(mapperErrorLuceneToEpisode(value))
  def mapperErrorLuceneToEpisode(value: org.apache.lucene.document.Document): HeminException =
    new HeminException(s"Error mapping Lucene Document to Episode : $value")

  def mapperFailureSolrToEpisode(value: SolrDocument): Try[Episode] = Failure(mapperErrorSolrToEpisode(value))
  def mapperErrorSolrToEpisode(value: SolrDocument): HeminException =
    new HeminException(s"Error mapping Solr Document to Episode : $value")

}
