package io.hemin.engine.domain

final case class ResultsWrapper(
  currPage: Int           = 0,
  maxPage: Int            = 0,
  totalHits: Int          = 0,
  results: List[IndexDoc] = Nil,
)
