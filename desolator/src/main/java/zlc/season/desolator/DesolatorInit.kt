package zlc.season.desolator

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.content.res.AssetManager
import android.database.Cursor
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import zlc.season.claritypotion.ClarityPotion
import zlc.season.desolator.hook.Hooker

@SuppressLint("StaticFieldLeak")
object DesolatorInit {
    val context = ClarityPotion.context
    val contextImpl = ClarityPotion.contextImpl

    val classLoader: ClassLoader = ClarityPotion.context.classLoader
    val assetManager: AssetManager = ClarityPotion.context.assets

    val activity: FragmentActivity?
        get() = with(ClarityPotion.activity) {
            if (this != null && this is FragmentActivity) {
                this
            } else {
                null
            }
        }
}