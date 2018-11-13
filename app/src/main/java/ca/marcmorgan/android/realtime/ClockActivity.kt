package ca.marcmorgan.android.realtime

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

class ClockActivity : AppCompatActivity() {

    var utcTimeTextView: TextView? = null
    var integralTimeTextView: TextView? = null
    var dstTimeTextView: TextView? = null
    var realTimeTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clock)

        utcTimeTextView = findViewById(R.id.clock_utc_time)
        integralTimeTextView = findViewById(R.id.clock_integral_time)
        dstTimeTextView = findViewById(R.id.clock_dst_time)
        realTimeTextView = findViewById(R.id.clock_realtime_time)

        updateTime()
    }

    private fun updateTime() {
        val blankTime = getString(R.string.empty_time)
        val format = DateTimeFormatter.ofPattern("h:mm:ss a")
        val utcTime = LocalTime.now(ZoneId.of("UTC"))

        utcTimeTextView?.text = utcTime.format(format)

        for(v in listOf(integralTimeTextView, dstTimeTextView, realTimeTextView)) {
            v?.text = blankTime
        }
    }
}
