package zlc.season.desolator

import android.content.Context
import dalvik.system.DexClassLoader
import zlc.season.desolator.DesolatorInit.Companion.assetManager
import zlc.season.desolator.DesolatorInit.Companion.context
import java.io.File

class ApkController {
    companion object {
        private const val ASSET_PLUGIN_PATH = "desolator_plugins"
        private const val PLUGIN_DEX_CACHE_DIR = "dex_cache"
    }

    private var defaultPluginId = 90000
    private val pluginDir = context.getDir("plugins", Context.MODE_PRIVATE)

    fun initInternalPlugin(): List<DesolatorPlugin> {
        try {
            val pluginDataList = mutableListOf<PluginData>()
            val fileNames = assetManager.list(ASSET_PLUGIN_PATH)
            fileNames?.forEach {
                if (it.endsWith(".apk")) {
                    val pluginData = parsePluginData(it)
                    copyInternalPlugin(it, pluginData)
                    pluginDataList.add(pluginData)
                }
            }
            val result = mutableListOf<DesolatorPlugin>()
            pluginDataList.forEach {
                val plugin = createPlugin(it)
                plugin?.let {
                    result.add(plugin)
                }
            }
            return result
        } catch (e: Exception) {
            e.logw()
            return emptyList()
        }
    }

    private fun parsePluginData(fileName: String): PluginData {
        val lastDotIndex = fileName.lastIndexOf('.')
        val str = fileName.substring(0, lastDotIndex)

        if (str.contains('_')) {
            val strList = str.split('_')
            if (strList.size == 3) {
                val pluginName = strList[0]
                val pluginId = try {
                    strList[1].toInt()
                } catch (e: Exception) {
                    createPluginId()
                }
                val pluginVersion = try {
                    strList[2].toInt()
                } catch (e: Exception) {
                    0
                }
                return PluginData(pluginId, pluginName, pluginVersion)
            }
        }

        val pluginId = createPluginId()
        return PluginData(pluginId, str, 0)
    }

    private fun createPluginId(): Int {
        defaultPluginId += 1
        return defaultPluginId
    }

    private fun copyInternalPlugin(fileName: String, pluginData: PluginData) {
        val outputFileDir = File(pluginDir, pluginData.id.toString())
        outputFileDir.checkDir()

        val inputStream = assetManager.open("$ASSET_PLUGIN_PATH/$fileName")
        val outputFile = File(outputFileDir, pluginData.fileName())
        inputStream.copy(outputFile)
    }

    fun createPlugin(pluginData: PluginData): DesolatorPlugin? {
        val pluginFileDir = File(pluginDir, pluginData.id.toString())
        if (pluginFileDir.exists() && pluginFileDir.isDirectory) {
            val dexCacheDir = File(pluginFileDir, PLUGIN_DEX_CACHE_DIR)
            if (!dexCacheDir.exists()) {
                dexCacheDir.mkdir()
            }
            val pluginFile = File(pluginFileDir, pluginData.fileName())
            val classLoader = DexClassLoader(
                pluginFile.path,
                dexCacheDir.path,
                null,
                DesolatorInit.classLoader
            )
            return DesolatorPlugin(pluginData.id, pluginFile.path, classLoader)
        }

        return null
    }
}