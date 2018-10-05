package io.disposia.engine.util.mapper

import java.time._
import java.util.Date


object DateMapper {

    val ZONE: ZoneId = ZoneId.of("Europe/Vienna")

    def asString(localDateTime: LocalDateTime): Option[String] = Option(localDateTime).map(_.toString)

    def asDate(localDate: LocalDate): Option[Date] = Option(localDate).map(ld => Date.from(ld.atStartOfDay(ZONE).toInstant))
    def asDate(zonedDateTime: ZonedDateTime): Option[Date] = Option(zonedDateTime).map(zdt => Date.from(zdt.toInstant))
    def asDate(localDateTime: LocalDateTime): Option[Date] = Option(localDateTime).map(ldt => Date.from(ldt.atZone(ZONE).toInstant))

}
