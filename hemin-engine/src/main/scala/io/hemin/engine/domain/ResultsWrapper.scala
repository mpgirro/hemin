package io.hemin.engine.domain

object ResultsWrapper {

  /** An "empty" [[io.hemin.engine.domain.ResultsWrapper]] representing no results found. */
  final val empty: ResultsWrapper = ResultsWrapper()

}

final case class ResultsWrapper(
  currPage: Int           = 0,
  maxPage: Int            = 0,
  totalHits: Int          = 0,
  results: List[IndexDoc] = Nil,
)
