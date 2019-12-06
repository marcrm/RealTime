package ca.marcmorgan.android.realtime.extensions

import org.threeten.bp.Duration
import kotlin.math.absoluteValue

fun Duration.prettyString(): String {
    val hours = seconds / (60 * 60)
    val hourRemainder = seconds.absoluteValue % (60 * 60)
    val minutes = hourRemainder / 60
    val secs = hourRemainder % 60

    return "${hours}h${minutes}m${secs}s"
}
