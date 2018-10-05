package io.disposia.engine.util.mapper

import java.sql.Timestamp
import java.time._


object DateMapper {

  val ZONE: ZoneId = ZoneId.of("Europe/Vienna")

  def asString(localDateTime: LocalDateTime): Option[String] = Option(localDateTime).map(_.toString)

  def asDate(value: LocalDate): Option[java.util.Date] = Option(value).map(_.atStartOfDay(ZONE)).map(_.toInstant).map(java.util.Date.from)
  def asDate(value: ZonedDateTime): Option[java.util.Date] = Option(value).map(_.toInstant).map(java.util.Date.from)
  def asDate(value: LocalDateTime): Option[java.util.Date] = Option(value).map(_.atZone(ZONE)).map(_.toInstant).map(java.util.Date.from)

  def asMilliseconds(value: LocalDateTime): Option[Long] = asZonedDateTime(value).map(_.toInstant).map(_.toEpochMilli)

  def asLocalDateTime(value: java.util.Date): Option[LocalDateTime] = Option(value).map(_.toInstant).map(LocalDateTime.ofInstant(_, ZONE))
  def asLocalDateTime(value: java.sql.Timestamp): Option[LocalDateTime] = Option(value).map(_.toLocalDateTime)
  def asLocalDateTime(value: ZonedDateTime): Option[LocalDateTime] = Option(value).map(LocalDateTime.from)
  def asLocalDateTime(milliseconds: Long): Option[LocalDateTime] = Option(milliseconds).map(Instant.ofEpochMilli).map(LocalDateTime.ofInstant(_, ZONE))
  def asLocalDateTime(localDateTime: String): Option[LocalDateTime] = Option(localDateTime).map(LocalDateTime.parse)

  def asSqlTimestamp(value: LocalDateTime): Option[java.sql.Timestamp] = Option(value).map(Timestamp.valueOf)

  def asLocalDate(value: java.util.Date): Option[LocalDate] = Option(value).map(_.toInstant).map(ZonedDateTime.ofInstant(_, ZONE)).map(_.toLocalDate)
  def asLocalDate(value: java.sql.Date): Option[LocalDate] = Option(value).map(_.toLocalDate)

  def asZonedDateTime(value: java.util.Date): Option[ZonedDateTime] = Option(value).map(_.toInstant).map(ZonedDateTime.ofInstant(_, ZONE))
  def asZonedDateTime(value: LocalDateTime): Option[ZonedDateTime] = Option(value).map(ZonedDateTime.ofInstant(_, ZoneOffset.UTC, ZONE))
  def asZonedDateTime(zonedDateTime: String): Option[ZonedDateTime] = Option(zonedDateTime).map(ZonedDateTime.parse)

  def asSqlDate(value: LocalDate): Option[java.sql.Date] = Option(value).map(java.sql.Date.valueOf)

}
