package hemin.engine.util

import java.time.ZoneId

object TimeUtil {

  val ZONE: ZoneId = ZoneId.of("Europe/Vienna")

  def now: Long = System.currentTimeMillis()

}
