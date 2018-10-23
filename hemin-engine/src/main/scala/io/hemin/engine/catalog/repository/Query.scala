package io.hemin.engine.catalog.repository

import io.hemin.engine.catalog.repository.BsonConversion.toDocument
import reactivemongo.bson.{BSONDocument, BSONValue}

object Query {

  /** Creates an instance as a `BSONDocument` from the elements map,
    * where only those tuples are addded to the stream who's value
    * (2nd tuple element) is not None, i.e. the value matches `Some(x)`
    *
    * @param elems All elements that are considered for the `BSONDocument`'s tuple stream
    */
  def apply(elems: Map[String, Option[BSONValue]]): BSONDocument = toDocument(elems)

  //def apply[A, B](elems: (A, B)*): Map[A,B] = Map(elems)
}
