package io.disposia.engine.catalog.mongo

import java.time.LocalDateTime

import io.disposia.engine.mapper.DateMapper
import reactivemongo.bson.{BSONBoolean, BSONDateTime, BSONInteger, BSONLong}

/**
  * @author max
  */
object BsonWrites {

    def toBson(b: Boolean): BSONBoolean = BSONBoolean(b)
    def toBson(i: Int): BSONInteger = BSONInteger(i)
    def toBson(l: Long): BSONLong = BSONLong(l)
    def toBson(d: LocalDateTime): BSONDateTime = BSONDateTime(DateMapper.INSTANCE.asMilliseconds(d))

}
