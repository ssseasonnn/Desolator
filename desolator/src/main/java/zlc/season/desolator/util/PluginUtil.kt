package zlc.season.desolator.util

import android.content.Context
import android.content.Context.MODE_PRIVATE
import zlc.season.desolator.DesolatorInit
import zlc.season.desolator.PluginData
import zlc.season.desolator.util.Constant.ASSET_DIR
import zlc.season.desolator.util.Constant.PLUGIN_NAME_PREFIX
import zlc.season.desolator.util.Constant.PLUGIN_NAME_SUFFIX
import zlc.season.desolator.util.Constant.STORAGE_DIR
import java.io.File

/**
 * Plugin name format: desolator_foo_1_1.apk
 */
fun String.isPluginAsset(): Boolean {
    return startsWith(PLUGIN_NAME_PREFIX) && endsWith(PLUGIN_NAME_SUFFIX)
}

fun PluginData.assetFileName(): String {
    return "${PLUGIN_NAME_PREFIX}_${name}_${id}_${version}${PLUGIN_NAME_SUFFIX}"
}

fun PluginData.assetPath(): String {
    return "$ASSET_DIR/${assetFileName()}"
}

fun PluginData.dexCacheDir(): File {
    return File(storageDir(), Constant.DEX_CACHE_DIR)
}

fun PluginData.storageFileName(): String {
    return "${name}_${version}.apk"
}

fun PluginData.storageDir(): File {
    val pluginDir = DesolatorInit.context.getDir(STORAGE_DIR, MODE_PRIVATE)
    return File(pluginDir, name)
}

fun PluginData.storageFile(): File {
    return File(storageDir(), storageFileName())
}