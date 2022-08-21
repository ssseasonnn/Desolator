package zlc.season.desolator

import android.annotation.SuppressLint
import android.content.res.AssetManager
import androidx.fragment.app.FragmentActivity
import zlc.season.claritypotion.ClarityPotion

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