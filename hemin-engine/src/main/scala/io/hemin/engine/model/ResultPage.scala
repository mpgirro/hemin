package io.hemin.engine.model

object ResultPage {

  /** An "empty" [[io.hemin.engine.model.ResultPage]] representing no results found. */
  final val empty: ResultPage = ResultPage()

}

final case class ResultPage(
  currPage: Int           = 0,
  maxPage: Int            = 0,
  totalHits: Int          = 0,
  results: List[IndexDoc] = Nil,
)
