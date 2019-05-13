package io.hemin.engine.catalog.repository

import reactivemongo.bson.{BSONDocument, BSONValue}

object Query {

  /** Creates a `BSONDocument` instance from the elements map,
    * where only those tuples are addded to the stream who's value
    * (2nd tuple element) is not None, i.e. the value matches `Some(x)`
    *
    * @param elems All elements that are considered for the `BSONDocument`'s tuple stream
    */
  def apply(elems: Map[String, Option[BSONValue]]): BSONDocument =
    BSONDocument.apply(elems.collect { case (k, Some(v)) => k -> v })

  def apply(elems: (String, Option[BSONValue])*): BSONDocument = apply(elems.toMap)

}
