package zlc.season.desolator

import android.content.Context
import android.content.Intent
import dalvik.system.DexClassLoader
import zlc.season.desolator.DesolatorInit.Companion.classLoader
import zlc.season.desolator.DesolatorInit.Companion.context
import java.io.File


object Desolator {
    private val PLUGIN_DIR = context.getDir("plugins", Context.MODE_PRIVATE)
    private const val PLUGIN_DEX_CACHE_DIR = "dex_cache"

    private val apkManager by lazy { ApkManager() }

    fun installPlugin(pluginData: PluginData) {
        val pluginInfo = pluginData.get()
        pluginInfo?.let {
            apkManager.addPlugin(it)
        }
    }

    fun startPlugin(pluginData: PluginData) {
        if (apkManager.isPluginLoaded(pluginData.id)) {
            val activityCls = classLoader.loadClass("com.example.plugina.PluginActivity") as Class<*>
            val intent = Intent(context, activityCls)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } else {
            "$pluginData has not install!".loge()
        }
    }

    private fun PluginData.get(): PluginInfo? {
        val apkFileDir = File(PLUGIN_DIR, id.toString())
        if (apkFileDir.isDirectory && apkFileDir.exists()) {
            val apkDexCacheDir = File(apkFileDir, PLUGIN_DEX_CACHE_DIR)
            if (!apkDexCacheDir.exists()) {
                apkDexCacheDir.mkdir()
            }
            val apkFile = File(apkFileDir, fileName())
            val classLoader = DexClassLoader(
                apkFile.path,
                apkDexCacheDir.path,
                null,
                classLoader
            )
            return PluginInfo(id, apkFile.path, classLoader)
        }
        return null
    }
}