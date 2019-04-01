package hemin.engine.model

object SearchResult {

  /** An "empty" [[hemin.engine.model.SearchResult]] representing no results found. */
  final val empty: SearchResult = SearchResult()

}

final case class SearchResult(
  currPage: Int           = 0,
  maxPage: Int            = 0,
  totalHits: Int          = 0,
  results: List[Document] = Nil,
)
