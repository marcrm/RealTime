package ca.marcmorgan.android.realtime

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

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

        for(v in listOf(utcTimeTextView, integralTimeTextView, dstTimeTextView, realTimeTextView)) {
            v?.text = blankTime
        }
    }
}
