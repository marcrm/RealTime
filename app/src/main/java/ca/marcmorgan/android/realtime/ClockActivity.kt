package ca.marcmorgan.android.realtime

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.location.*
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter

class ClockActivity : AppCompatActivity() {

    private companion object {
        const val UPDATE_MS: Long = 100
        const val LOC_UPDATE_MS: Long = 10 * 1000
        const val LOC_UPDATE_FASTEST: Long = 2 * 1000
        const val CHECK_LOCATION_PERMISSION = 1
        const val TAG = "ClockActivity"
    }

    private lateinit var utcTimeTextView: TextView
    private lateinit var locationDurationTextView: TextView
    private lateinit var dstDurationTextView: TextView
    private lateinit var tzIntegralNoDstTextView: TextView
    private lateinit var tzIntegralDstTextView: TextView
    private lateinit var dstIntegralTextView: TextView
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
        locationDurationTextView = findViewById(R.id.clock_location_duration)
        dstDurationTextView = findViewById(R.id.clock_dst_duration)
        tzIntegralNoDstTextView = findViewById(R.id.clock_tz_integral_no_dst_time)
        tzIntegralDstTextView = findViewById(R.id.clock_tz_integral_dst_time)
        dstIntegralTextView = findViewById(R.id.clock_dst_integral_time)
        realTimeTextView = findViewById(R.id.clock_realtime_time)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        checkLocationPermissions()
        initLocation()
        updateTime()
        initTimer()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_clock, menu)
        return true
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdate()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_clock_credits -> {
                showCredits()
                true
            }
            else -> false
        }
    }

    private fun showCredits() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.credits_title)
            .setMessage(R.string.credits)
            .setPositiveButton(R.string.ok, null)
            .create()
        dialog.show()
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

    private fun updateTime() {
        val blankTime = getString(R.string.empty_time)
        val format = DateTimeFormatter.ofPattern("h:mm:ss a")
        val now = ZonedDateTime.now()
        val calculator = TimeCalculator(now, location, format, blankTime)

        utcTimeTextView.text = calculator.utcTimeText
        locationDurationTextView.text = calculator.locationDurationText
        dstDurationTextView.text = calculator.dstDurationText
        tzIntegralNoDstTextView.text = calculator.integralTimeText
        tzIntegralDstTextView.text = calculator.integralTimeDstText
        dstIntegralTextView.text = calculator.dstTimeText
        realTimeTextView.text = calculator.realTimeText
    }
}
