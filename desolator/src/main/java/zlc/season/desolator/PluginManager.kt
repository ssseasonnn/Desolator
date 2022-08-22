package zlc.season.desolator

import com.google.gson.Gson
import dalvik.system.DexClassLoader
import zlc.season.desolator.DesolatorHelper.assetManager
import zlc.season.desolator.DesolatorHelper.classLoader
import zlc.season.desolator.util.*
import zlc.season.desolator.util.Constant.ASSET_DIR
import zlc.season.desolator.util.Constant.PLUGIN_INFO_FILE_NAME
import zlc.season.desolator.util.Constant.PLUGIN_NAME_SUFFIX
import java.io.InputStream

class PluginManager {
    private val gson = Gson()

    fun initInternalPlugin(): List<DesolatorPlugin> {
        return try {
            val pluginDataList = createPluginDataList()
            createPluginList(pluginDataList)
        } catch (e: Exception) {
            e.logw()
            emptyList()
        }
    }

    fun createPlugin(pluginData: PluginData): DesolatorPlugin? {
        val pluginFile = pluginData.pluginFile()
        if (!pluginFile.exists()) return null

        val dexCacheDir = pluginData.dexCacheDir()
        val classLoader = DexClassLoader(
            pluginFile.path,
            dexCacheDir.path,
            null,
            classLoader
        )
        return DesolatorPlugin(pluginData.id, pluginFile.path, classLoader)
    }

    private fun createPluginDataList(): List<PluginData> {
        val paths = listAsset(ASSET_DIR) ?: return emptyList()

        val pluginDataList = mutableListOf<PluginData>()
        paths.forEach { path ->
            if (path.isPluginDir()) {
                val pluginData = parsePluginData(path)
                pluginData?.let {
                    if (DesolatorHelper.isDebug || shouldUpdateDiskFile(it)) {
                        deleteOldDiskFile(pluginData)
                        copyFileFromAssetToDisk(path, pluginData)
                    }
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
            if (name == PLUGIN_INFO_FILE_NAME) {
                containPluginInfoFile = true
                break
            }
        }
        return containPluginInfoFile
    }


    private fun parsePluginData(path: String): PluginData? {
        val inputStream = openAsset("$ASSET_DIR/$path/$PLUGIN_INFO_FILE_NAME")
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
        pluginDataList.forEach { pluginData ->
            val plugin = createPlugin(pluginData)
            plugin?.let {
                result.add(it)
            }
        }
        return result
    }

    private fun shouldUpdateDiskFile(pluginData: PluginData): Boolean {
        val pluginFile = pluginData.pluginFile()
        return !pluginFile.exists()
    }

    private fun copyFileFromAssetToDisk(path: String, pluginData: PluginData) {
        val assetFile = openAsset("$ASSET_DIR/$path/${pluginData.name}${PLUGIN_NAME_SUFFIX}")
        val pluginFile = pluginData.pluginFile()
        assetFile copyTo pluginFile
    }

    private fun deleteOldDiskFile(pluginData: PluginData) {
        val dir = pluginData.pluginBaseDir()
        dir.listFiles()?.forEach {
            it.deleteRecursively()
        }
    }

    private fun listAsset(path: String): Array<String>? {
        return assetManager.list(path)
    }

    private fun openAsset(path: String): InputStream {
        return assetManager.open(path)
    }
}