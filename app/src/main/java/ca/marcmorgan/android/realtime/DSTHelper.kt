package ca.marcmorgan.android.realtime

import org.threeten.bp.DayOfWeek
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.TemporalAdjusters

class DSTHelper {
    companion object {
        val FIRST_SUNDAY = TemporalAdjusters.firstInMonth(DayOfWeek.SUNDAY)
    }

    private fun dstBegin(year: Int) = LocalDate.of(year, 3, 1).with(FIRST_SUNDAY).plusWeeks(1).atStartOfDay()

    private fun dstEnd(year: Int) = LocalDate.of(year, 11, 1).with(FIRST_SUNDAY).atStartOfDay()

    fun dstToSeconds(now: LocalDateTime): Duration {
        val dstStartThis = dstBegin(now.year)
        val dstEndThis = dstEnd(now.year)
        val dstEndPrev = dstEnd(now.minusYears(1).year)
        val dstStartNext = dstBegin(now.plusYears(1).year)

        val (start, end, isDst) = when {
            now < dstStartThis -> Triple(dstEndPrev, dstStartThis, false)
            now < dstEndThis -> Triple(dstStartThis, dstEndThis, true)
            else -> Triple(dstEndThis, dstStartNext, false)
        }

        val totalPeriod = Duration.between(start, end).toMillis().toDouble()
        val thisPeriod = Duration.between(start, now).toMillis().toDouble()
        val completion = thisPeriod / totalPeriod
        val hour = 60.0 * 60.0 * 1000.0
        val adjustment = completion * hour

        return when {
            isDst -> hour - adjustment
            else -> adjustment
        }.let { Duration.ofMillis(it.toLong()) }
    }
}
