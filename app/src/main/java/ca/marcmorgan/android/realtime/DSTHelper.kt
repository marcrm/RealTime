package ca.marcmorgan.android.realtime

import org.threeten.bp.*
import kotlin.math.absoluteValue

class DSTHelper {
    fun dstToSeconds(now: ZonedDateTime): Duration {
        val tz = now.zone
        val prevTransition = tz.rules.previousTransition(now.toInstant())
        val nextTransition = tz.rules.nextTransition(now.toInstant())

        val start = prevTransition.dateTimeAfter
        val end = nextTransition.dateTimeBefore
        val totalPeriod = Duration.between(start, end).toMillis().toDouble()
        val thisPeriod = Duration.between(start, now).toMillis().toDouble()
        val completion = thisPeriod / totalPeriod

        val offset = (nextTransition.offsetAfter.totalSeconds - nextTransition.offsetBefore.totalSeconds) * 1000
        val adjustment = completion * offset

        return when {
            adjustment < 0 -> offset.absoluteValue + adjustment
            else -> adjustment
        }.let { Duration.ofMillis(it.toLong()) }
    }
}
