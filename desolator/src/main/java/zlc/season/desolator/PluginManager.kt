package zlc.season.desolator

import com.google.gson.Gson
import dalvik.system.DexClassLoader
import zlc.season.desolator.util.*
import zlc.season.desolator.util.Constant.ASSET_DIR
import zlc.season.desolator.util.Constant.PLUGIN_INFO_FILE_NAME

class PluginManager {
    val gson = Gson()

    fun initInternalPlugin(): List<DesolatorPlugin> {
        return try {
            val pluginDataList = createPluginDataList()
            createPluginList(pluginDataList)
        } catch (e: Exception) {
            e.logw()
            emptyList()
        }
    }

    private fun createPluginDataList(): List<PluginData> {
        val paths = listAsset(ASSET_DIR) ?: return emptyList()

        val pluginDataList = mutableListOf<PluginData>()
        paths.forEach { path ->
            if (path.isPluginDir()) {
                val pluginData = parsePluginData(path)
                pluginData?.let {
                    copyPluginFromAssetToStorage(path, pluginData)
                    pluginDataList.add(pluginData)
                }
            }
        }
        return pluginDataList
    }

    private fun String.isPluginDir(): Boolean {
        val paths = listAsset("$ASSET_DIR/$this") ?: return false

        var containPluginInfoFile = false
        for (name in paths) {
            if (name == Constant.PLUGIN_INFO_FILE_NAME) {
                containPluginInfoFile = true
                break
            }
        }
        return containPluginInfoFile
    }

    private fun listAsset(path: String): Array<String>? {
        return DesolatorInit.assetManager.list(path)
    }

    private fun parsePluginData(path: String): PluginData? {
        val inputStream = DesolatorInit.assetManager.open("$ASSET_DIR/$path/$PLUGIN_INFO_FILE_NAME")
        val json = String(inputStream.readBytes())
        val pluginData = try {
            gson.fromJson(json, PluginData::class.java)
        } catch (e: Exception) {
            null
        }
        return pluginData
    }

    private fun createPluginList(pluginDataList: List<PluginData>): List<DesolatorPlugin> {
        val result = mutableListOf<DesolatorPlugin>()
        pluginDataList.forEach {
            val plugin = createPlugin(it)
            plugin?.let {
                result.add(it)
            }
        }
        return result
    }

    private fun copyPluginFromAssetToStorage(path: String, pluginData: PluginData) {
        val assetFileInputStream =
            DesolatorInit.assetManager.open("$ASSET_DIR/$path/${pluginData.name}.apk")

        val storageFile = pluginData.storageFile()
        assetFileInputStream.copy(storageFile)
    }

    fun createPlugin(pluginData: PluginData): DesolatorPlugin? {
        val pluginFile = pluginData.storageFile()
        if (!pluginFile.exists()) return null

        val dexCacheDir = pluginData.dexCacheDir()
        dexCacheDir.checkDir()

        val classLoader = DexClassLoader(
            pluginFile.path,
            dexCacheDir.path,
            null,
            DesolatorInit.classLoader
        )
        return DesolatorPlugin(pluginData.id, pluginFile.path, classLoader)
    }
}