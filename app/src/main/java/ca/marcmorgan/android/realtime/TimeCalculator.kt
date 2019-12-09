package ca.marcmorgan.android.realtime

import android.location.Location
import ca.marcmorgan.android.realtime.extensions.prettyString
import org.threeten.bp.Duration
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import kotlin.math.absoluteValue

class TimeCalculator(private val now: ZonedDateTime, private val location: Location?,
                     private val timeFormatter: DateTimeFormatter, private val blankTime: String) {

    private val zoneRules = now.zone.rules
    private val nowInstant = now.toInstant()

    private val locationDuration by lazy {
        location?.let{ Duration.ofMillis(((it.longitude / 180.0) * 12 * 60 * 60 * 1000).toLong()) }
    }
    private val dstDuration by lazy {
        val prevTransition = zoneRules.previousTransition(now.toInstant())
        val nextTransition = zoneRules.nextTransition(now.toInstant())

        val start = prevTransition.dateTimeAfter
        val end = nextTransition.dateTimeBefore
        val totalPeriod = Duration.between(start, end).toMillis().toDouble()
        val thisPeriod = Duration.between(start, now).toMillis().toDouble()
        val completion = thisPeriod / totalPeriod

        val offset = (nextTransition.offsetAfter.totalSeconds - nextTransition.offsetBefore.totalSeconds) * 1000
        val adjustment = completion * offset

        when {
            adjustment < 0 -> offset.absoluteValue + adjustment
            else -> adjustment
        }.let { Duration.ofMillis(it.toLong()) }
    }

    private val utcTime by lazy { now.withZoneSameInstant(ZoneOffset.UTC) }
    private val dstTime by lazy {
        val nowNoDst = now.withZoneSameInstant(zoneRules.getStandardOffset(nowInstant))
        nowNoDst.plus(dstDuration)
    }
    private val integralTime by lazy { locationDuration?.let { utcTime.plus(it) } }
    private val integralTimeDst by lazy {
        integralTime?.plus(zoneRules.getDaylightSavings(nowInstant))
    }
    private val realTime by lazy { locationDuration?.let { utcTime.plus(it + dstDuration) } }

    private fun nullTimeFormat(time: ZonedDateTime?) = time?.format(timeFormatter) ?: blankTime

    val utcTimeText: String
        get() = utcTime.format(timeFormatter)
    val locationDurationText: String
        get() = locationDuration?.prettyString() ?: blankTime
    val dstDurationText: String
        get() = dstDuration.prettyString()
    val integralTimeText: String
        get() = nullTimeFormat(integralTime)
    val integralTimeDstText: String
        get() = nullTimeFormat(integralTimeDst)
    val dstTimeText: String
        get() = dstTime.format(timeFormatter)
    val realTimeText: String
        get() = nullTimeFormat(realTime)
}
