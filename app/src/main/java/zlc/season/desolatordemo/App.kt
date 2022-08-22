package zlc.season.desolatordemo

import android.app.Application
import zlc.season.desolator.Desolator

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Desolator.init(BuildConfig.DEBUG)
    }
}