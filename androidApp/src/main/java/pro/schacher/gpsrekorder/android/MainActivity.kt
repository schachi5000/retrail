package pro.schacher.gpsrekorder.android

import android.os.Bundle
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import pro.schacher.gpsrekorder.App
import pro.schacher.gpsrekorder.shared.database.DatabaseDriverFactory
import pro.schacher.gpsrekorder.shared.location.AndroidLocationDataSource

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val locationDataSource = AndroidLocationDataSource(this)
        setContent {
            App(locationDataSource, DatabaseDriverFactory(this))
        }

        window.addFlags(FLAG_KEEP_SCREEN_ON)
    }
}
