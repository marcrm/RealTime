package ca.marcmorgan.android.realtime

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

class ClockActivity : AppCompatActivity() {

    private companion object {
        const val UPDATE_MS: Long = 100
        const val TAG = "ClockActivity"
    }

    private lateinit var utcTimeTextView: TextView
    private lateinit var integralTimeTextView: TextView
    private lateinit var dstTimeTextView: TextView
    private lateinit var realTimeTextView: TextView
    private val refreshHandler = Handler()

    private var location: Location? = null
    private val locationListener = object : LocationListener {
        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
        override fun onProviderEnabled(p0: String?) {}
        override fun onProviderDisabled(p0: String?) {}
        override fun onLocationChanged(p0: Location?) {
            location = p0
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clock)

        utcTimeTextView = findViewById(R.id.clock_utc_time)
        integralTimeTextView = findViewById(R.id.clock_integral_time)
        dstTimeTextView = findViewById(R.id.clock_dst_time)
        realTimeTextView = findViewById(R.id.clock_realtime_time)

        initLocation()
        updateTime()
        initTimer()
    }

    private fun initLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        try {
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
        } catch (ex: SecurityException) {
            Log.d(TAG, "location not permitted")
        }
    }

    private fun initTimer() {
        val updateRunnable = Runnable {
            updateTime()
            initTimer()
        }
        refreshHandler.postDelayed(updateRunnable, UPDATE_MS)
    }

    private fun longitudeToSeconds(loc: Location) = (loc.longitude / 180.0) * 12 * 60 * 60

    private fun updateTime() {
        val blankTime = getString(R.string.empty_time)
        val format = DateTimeFormatter.ofPattern("h:mm:ss a")
        val utcTime = LocalTime.now(ZoneId.of("UTC"))
        val integralTime = location?.let {
            utcTime.plusSeconds(longitudeToSeconds(it).toLong())
        }

        utcTimeTextView.text = utcTime.format(format)
        integralTimeTextView.text = integralTime?.format(format) ?: blankTime
        dstTimeTextView.text = blankTime
        realTimeTextView.text = blankTime
    }
}
