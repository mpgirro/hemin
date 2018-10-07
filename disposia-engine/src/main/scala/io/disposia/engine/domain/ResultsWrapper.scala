package io.disposia.engine.domain

case class ResultsWrapper(
  currPage: Int           = 0,
  maxPage: Int            = 0,
  totalHits: Int          = 0,
  results: List[IndexDoc] = List(),
)
