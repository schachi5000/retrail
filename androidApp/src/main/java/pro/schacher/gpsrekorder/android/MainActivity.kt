package pro.schacher.gpsrekorder.android

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import pro.schacher.gpsrekorder.App
import pro.schacher.gpsrekorder.shared.hasPermission
import pro.schacher.gpsrekorder.shared.location.AndroidLocationDataSource

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = this.applicationContext
        setContent {
            App(AndroidLocationDataSource(context))
        }
    }

    override fun onResume() {
        super.onResume()

        if (!this.hasPermission(ACCESS_FINE_LOCATION)) {
            this.requestPermissions(
                arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION), 42
            )
        }
    }
}
