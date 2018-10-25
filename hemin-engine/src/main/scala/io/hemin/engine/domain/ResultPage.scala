package io.hemin.engine.domain

object ResultPage {

  /** An "empty" [[io.hemin.engine.domain.ResultPage]] representing no results found. */
  final val empty: ResultPage = ResultPage()

}

final case class ResultPage(
  currPage: Int           = 0,
  maxPage: Int            = 0,
  totalHits: Int          = 0,
  results: List[IndexDoc] = Nil,
)
