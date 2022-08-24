package zlc.season.desolator.util

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import zlc.season.desolator.DesolatorHelper.context
import zlc.season.desolator.PluginData
import zlc.season.desolator.util.Constant.DISK_DIR
import zlc.season.desolator.util.Constant.PLUGIN_NAME_SUFFIX
import java.io.File

// data/data/com.xxx.xxx/app_desolator_plugins
internal fun appDir(): File {
    return context.getDir(DISK_DIR, MODE_PRIVATE)
}

// data/data/com.xxx.xxx/app_desolator_plugins/id
fun PluginData.pluginBaseDir(): File {
    val dir = File(appDir(), id)
    dir.checkDir()
    return dir
}

// data/data/com.xxx.xxx/app_desolator_plugins/id/version
fun PluginData.pluginDir(): File {
    val dir = File(pluginBaseDir(), version)
    dir.checkDir()
    return dir
}

// data/data/com.xxx.xxx/app_desolator_plugins/id/version/name.apk
fun PluginData.pluginFile(): File {
    return File(pluginDir(), pluginFileName())
}

// name.apk
fun PluginData.pluginFileName(): String {
    return "$name${PLUGIN_NAME_SUFFIX}"
}

fun PluginData.dexCacheDir(): File {
    val dir = File(pluginDir(), Constant.DEX_CACHE_DIR)
    dir.checkDir()
    return dir
}

fun PluginData.isPluginExists(): Boolean {
    return pluginFile().exists()
}

fun PluginData.createEntranceFragment(
    activity: FragmentActivity,
    params: Bundle? = null
): Fragment {
    val fragment = activity.supportFragmentManager.fragmentFactory.instantiate(
        activity.classLoader,
        entrance
    )
    params?.apply {
        fragment.arguments = this
    }
    return fragment
}