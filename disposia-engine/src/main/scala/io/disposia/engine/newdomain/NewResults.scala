package io.disposia.engine.newdomain

case class NewResults(
  currPage: Int              = 0,
  maxPage: Int               = 0,
  totalHits: Int             = 0,
  results: List[NewIndexDoc] = List()
)
