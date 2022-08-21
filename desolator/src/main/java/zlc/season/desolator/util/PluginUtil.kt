package zlc.season.desolator.util

import android.content.Context.MODE_PRIVATE
import zlc.season.desolator.DesolatorInit
import zlc.season.desolator.PluginData
import zlc.season.desolator.util.Constant.DISK_DIR
import zlc.season.desolator.util.Constant.PLUGIN_NAME_SUFFIX
import java.io.File

fun PluginData.dexCacheDir(): File {
    val dir = File(pluginDir(), Constant.DEX_CACHE_DIR)
    dir.checkDir()
    return dir
}

fun PluginData.pluginBaseDir(): File {
    val pluginDir = DesolatorInit.context.getDir(DISK_DIR, MODE_PRIVATE)
    val dir = File("${pluginDir.path}/${name}")
    dir.checkDir()
    return dir
}

fun PluginData.pluginDir(): File {
    val dir = File(pluginBaseDir(), version.toString())
    dir.checkDir()
    return dir
}

fun PluginData.pluginFile(): File {
    return File(pluginDir(), "$name${PLUGIN_NAME_SUFFIX}")
}