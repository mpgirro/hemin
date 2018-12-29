package io.hemin.engine.util.mapper

import java.sql.Timestamp
import java.time._

import io.hemin.engine.util.TimeUtil


object DateMapper {

  def asString(value: LocalDateTime): Option[String] = Option(value)
    .map(_.toString)

  def asString(value: ZonedDateTime): Option[String] = Option(value)
    .map(_.toString)

  def asDate(value: LocalDate): Option[java.util.Date] = Option(value)
    .map(_.atStartOfDay(TimeUtil.ZONE))
    .map(_.toInstant)
    .map(java.util.Date.from)

  def asDate(value: ZonedDateTime): Option[java.util.Date] = Option(value)
    .map(_.toInstant)
    .map(java.util.Date.from)

  def asDate(value: LocalDateTime): Option[java.util.Date] = Option(value)
    .map(_.atZone(TimeUtil.ZONE))
    .map(_.toInstant)
    .map(java.util.Date.from)

  def asMilliseconds(value: LocalDateTime): Option[Long] = asZonedDateTime(value)
    .flatMap(asMilliseconds)

  def asMilliseconds(value: ZonedDateTime): Option[Long] = Option(value)
    .map(_.toInstant)
    .map(_.toEpochMilli)

  def asMilliseconds(value: java.util.Date): Option[Long] = Option(value)
    .map(_.getTime)

  def asMilliseconds(value: String): Option[Long] = asLocalDateTime(value)
    .flatMap(asMilliseconds)

  def asLocalDateTime(value: java.util.Date): Option[LocalDateTime] = Option(value)
    .map(_.toInstant)
    .map(LocalDateTime.ofInstant(_, TimeUtil.ZONE))

  def asLocalDateTime(value: java.sql.Timestamp): Option[LocalDateTime] = Option(value)
    .map(_.toLocalDateTime)

  def asLocalDateTime(value: ZonedDateTime): Option[LocalDateTime] = Option(value)
    .map(LocalDateTime.from)

  def asLocalDateTime(value: Long): Option[LocalDateTime] = Option(value)
    .map(Instant.ofEpochMilli)
    .map(LocalDateTime.ofInstant(_, TimeUtil.ZONE))

  def asLocalDateTime(value: String): Option[LocalDateTime] = Option(value)
    .map(LocalDateTime.parse)

  def asSqlTimestamp(value: LocalDateTime): Option[java.sql.Timestamp] = Option(value)
    .map(Timestamp.valueOf)

  def asLocalDate(value: java.util.Date): Option[LocalDate] = Option(value)
    .map(_.toInstant)
    .map(ZonedDateTime.ofInstant(_, TimeUtil.ZONE))
    .map(_.toLocalDate)

  def asLocalDate(value: java.sql.Date): Option[LocalDate] = Option(value)
    .map(_.toLocalDate)

  def asZonedDateTime(value: java.util.Date): Option[ZonedDateTime] = Option(value)
    .map(_.toInstant)
    .map(ZonedDateTime.ofInstant(_, TimeUtil.ZONE))

  def asZonedDateTime(value: LocalDateTime): Option[ZonedDateTime] = Option(value)
    .map(ZonedDateTime.ofInstant(_, ZoneOffset.UTC, TimeUtil.ZONE))

  def asZonedDateTime(value: String): Option[ZonedDateTime] = Option(value)
    .map(ZonedDateTime.parse)

  def asZonedDateTime(value: Long): Option[ZonedDateTime] = asLocalDateTime(value)
    .flatMap(asZonedDateTime)

  def asSqlDate(value: LocalDate): Option[java.sql.Date] = Option(value)
    .map(java.sql.Date.valueOf)

}
