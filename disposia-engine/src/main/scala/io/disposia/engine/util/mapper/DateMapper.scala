package io.disposia.engine.util.mapper

import java.sql.Timestamp
import java.time._


object DateMapper {

  val ZONE: ZoneId = ZoneId.of("Europe/Vienna")

  def asString(localDateTime: LocalDateTime): Option[String] = Option(localDateTime).map(_.toString)

  def asDate(value: LocalDate): Option[java.util.Date] = Option(value).map(x => java.util.Date.from(x.atStartOfDay(ZONE).toInstant))
  def asDate(value: ZonedDateTime): Option[java.util.Date] = Option(value).map(x => java.util.Date.from(x.toInstant))
  def asDate(value: LocalDateTime): Option[java.util.Date] = Option(value).map(x => java.util.Date.from(x.atZone(ZONE).toInstant))

  def asMilliseconds(value: LocalDateTime): Option[Long] = asZonedDateTime(value).map(_.toInstant).map(_.toEpochMilli)

  def asLocalDateTime(value: java.util.Date): Option[LocalDateTime] = Option(LocalDateTime.ofInstant(value.toInstant, ZONE))
  def asLocalDateTime(value: java.sql.Timestamp): Option[LocalDateTime] = Option(value.toLocalDateTime)
  def asLocalDateTime(value: ZonedDateTime): Option[LocalDateTime] = Option(LocalDateTime.from(value))
  def asLocalDateTime(milliseconds: Long): Option[LocalDateTime] = Option(LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZONE))
  def asLocalDateTime(localDateTime: String): Option[LocalDateTime] = Option(LocalDateTime.parse(localDateTime))

  def asSqlTimestamp(value: LocalDateTime): Option[java.sql.Timestamp] = Option(Timestamp.valueOf(value))

  def asLocalDate(value: java.util.Date): Option[LocalDate] = Option(ZonedDateTime.ofInstant(value.toInstant, ZONE).toLocalDate)
  def asLocalDate(value: java.sql.Date): Option[LocalDate] = Option(value.toLocalDate)

  def asZonedDateTime(value: java.util.Date): Option[ZonedDateTime] = Option(ZonedDateTime.ofInstant(value.toInstant, ZONE))
  def asZonedDateTime(value: LocalDateTime): Option[ZonedDateTime] = Option(ZonedDateTime.ofInstant(value, ZoneOffset.UTC, ZONE))
  def asZonedDateTime(zonedDateTime: String): Option[ZonedDateTime] = Option(ZonedDateTime.parse(zonedDateTime))

  def asSqlDate(value: LocalDate): Option[java.sql.Date] = Option(java.sql.Date.valueOf(value))

}
