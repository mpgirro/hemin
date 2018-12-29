package io.hemin.engine.util

import java.time.{ZoneId, ZonedDateTime}

object TimeUtil {

  val ZONE: ZoneId = ZoneId.of("Europe/Vienna")

  def now(): ZonedDateTime = ZonedDateTime.now(ZONE)

}
