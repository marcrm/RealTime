package ca.marcmorgan.android.realtime

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class RealTimeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
}
