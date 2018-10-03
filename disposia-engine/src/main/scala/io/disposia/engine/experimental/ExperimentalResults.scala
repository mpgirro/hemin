package io.disposia.engine.experimental

import io.disposia.engine.domain.IndexDoc

case class ExperimentalResults (
  currPage: Int = 0,
  maxPage: Int = 0,
  totalHits: Int = 0,
  results: List[IndexDoc] = List()
)
