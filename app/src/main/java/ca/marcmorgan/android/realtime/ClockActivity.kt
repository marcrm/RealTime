package ca.marcmorgan.android.realtime

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.TextView
import com.google.android.gms.location.*
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.TemporalAdjusters

class ClockActivity : AppCompatActivity() {

    private companion object {
        const val UPDATE_MS: Long = 100
        const val LOC_UPDATE_MS: Long = 10 * 1000
        const val LOC_UPDATE_FASTEST: Long = 2 * 1000
        const val CHECK_LOCATION_PERMISSION = 1
        const val TAG = "ClockActivity"
    }

    private lateinit var utcTimeTextView: TextView
    private lateinit var integralTimeTextView: TextView
    private lateinit var dstTimeTextView: TextView
    private lateinit var realTimeTextView: TextView
    private val refreshHandler = Handler()

    private var location: Location? = null
    private val locationRequest = LocationRequest().apply {
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        interval = LOC_UPDATE_MS
        fastestInterval = LOC_UPDATE_FASTEST
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.lastLocation?.let {
                Log.d(TAG, "New location: $it")
                location = it
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clock)

        utcTimeTextView = findViewById(R.id.clock_utc_time)
        integralTimeTextView = findViewById(R.id.clock_integral_time)
        dstTimeTextView = findViewById(R.id.clock_dst_time)
        realTimeTextView = findViewById(R.id.clock_realtime_time)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        checkLocationPermissions()
        initLocation()
        updateTime()
        initTimer()
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdate()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun dstToSeconds(): Double {
        val now = LocalDateTime.now()
        val blank = now.toLocalDate().atStartOfDay()
        val firstSunday = TemporalAdjusters.firstInMonth(DayOfWeek.SUNDAY)
        val dstStartThis = blank.withMonth(3).with(firstSunday).plusWeeks(1)
        val dstEndThis = blank.withMonth(11).with(firstSunday)
        val dstEndPrev = blank.minusYears(1).withMonth(11).with(firstSunday)
        val dstStartNext = blank.plusYears(1).withMonth(3).with(firstSunday).plusWeeks(1)

        val (start, end, isDst) = when {
            now < dstStartThis -> Triple(dstEndPrev, dstStartThis, false)
            now < dstEndThis -> Triple(dstStartThis, dstEndThis, true)
            else -> Triple(dstEndThis, dstStartNext, false)
        }

        val totalPeriod = Duration.between(start, end).toMillis().toDouble()
        val thisPeriod = Duration.between(start, now).toMillis().toDouble()
        val completion = thisPeriod / totalPeriod
        val hour = 60.0 * 60.0
        val adjustment = completion * hour

        return when {
            isDst -> hour - adjustment
            else -> adjustment
        }
    }

    private fun checkLocationPermissions() {
        val allowed = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if(allowed == PackageManager.PERMISSION_GRANTED) {
            return
        }

        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), CHECK_LOCATION_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            CHECK_LOCATION_PERMISSION -> {
                initLocation()
                stopLocationUpdates()
                startLocationUpdate()
            }
            else -> {}
        }
    }

    private fun initLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                lastLoc?.let { location = it }
            }
        } catch(ex: SecurityException) {
            Log.d(TAG, "location not permitted")
        }
    }

    private fun startLocationUpdate() {
        Log.d(TAG, "starting location updates")
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } catch(ex: SecurityException) {
            Log.d(TAG, "location not permitted")
        }
    }

    private fun stopLocationUpdates() {
        Log.d(TAG, "stopping location updates")
        fusedLocationClient.removeLocationUpdates(locationCallback)
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

        val integralSeconds = location?.let {
            longitudeToSeconds(it).toLong()
        }
        val dstSeconds = dstToSeconds().toLong()

        val integralTime = integralSeconds?.let {
            utcTime.plusSeconds(it)
        }
        val dstTime = utcTime.plusSeconds(dstSeconds)
        val realTime = integralSeconds?.let {
            utcTime.plusSeconds(it + dstSeconds)
        }

        utcTimeTextView.text = utcTime.format(format)
        integralTimeTextView.text = integralTime?.format(format) ?: blankTime
        dstTimeTextView.text = dstTime.format(format)
        realTimeTextView.text = realTime?.format(format) ?: blankTime
    }
}
