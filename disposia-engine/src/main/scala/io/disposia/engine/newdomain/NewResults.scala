package io.disposia.engine.newdomain

import io.disposia.engine.olddomain.OldIndexDoc

case class NewResults(
  currPage: Int           = 0,
  maxPage: Int            = 0,
  totalHits: Int          = 0,
  results: List[NewIndexDoc] = List()
)
