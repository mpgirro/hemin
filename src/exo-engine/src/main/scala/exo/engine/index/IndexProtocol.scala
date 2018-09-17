package exo.engine.index

import exo.engine.domain.dto.{IndexDocDTO, ResultWrapperDTO}

/**
  * @author Maximilian Irro
  */
object IndexProtocol {

    trait IndexEvent

    // Crawler/Parser/CatalogStore -> IndexStore
    case class AddDocIndexEvent(doc: IndexDocDTO) extends IndexEvent
    case class UpdateDocWebsiteDataIndexEvent(exo: String, html: String) extends IndexEvent
    case class UpdateDocImageIndexEvent(exo: String, image: String) extends IndexEvent
    case class UpdateDocLinkIndexEvent(exo: String, newLink: String) extends IndexEvent


    trait IndexCommand

    // IndexStore -> IndexStore
    case class CommitIndex() extends IndexCommand


    trait IndexQuery

    // Searcher -> IndexStore
    case class SearchIndex(query: String, page: Int, size: Int) extends IndexQuery


    trait IndexQueryResult

    // IndexStore -> Searcher
    case class IndexResultsFound(query: String, results: ResultWrapperDTO) extends IndexQueryResult
    case class NoIndexResultsFound(query: String) extends IndexQueryResult

}
