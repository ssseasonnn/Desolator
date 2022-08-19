package zlc.season.desolator

import dalvik.system.DexClassLoader
import zlc.season.desolator.DesolatorInit.Companion.assetManager
import zlc.season.desolator.util.*
import zlc.season.desolator.util.Constant.ASSET_DIR

class PluginManager {
    fun initInternalPlugin(): List<DesolatorPlugin> {
        return try {
            val pluginDataList = createPluginDataList()
            createPluginList(pluginDataList)
        } catch (e: Exception) {
            e.logw()
            emptyList()
        }
    }

    private fun createPluginDataList(): MutableList<PluginData> {
        val pluginDataList = mutableListOf<PluginData>()

        val assetFileNames = assetManager.list(ASSET_DIR)
        assetFileNames?.forEach {
            if (it.isPluginAsset()) {
                val pluginData = it.parsePluginData()
                pluginData?.let {
                    copyPluginFromAssetToStorage(pluginData)
                    pluginDataList.add(pluginData)
                }
            }
        }
        return pluginDataList
    }

    private fun createPluginList(pluginDataList: MutableList<PluginData>): List<DesolatorPlugin> {
        val result = mutableListOf<DesolatorPlugin>()
        pluginDataList.forEach {
            val plugin = createPlugin(it)
            plugin?.let {
                result.add(it)
            }
        }
        return result
    }

    private fun copyPluginFromAssetToStorage(pluginData: PluginData) {
        val assetFileInputStream = assetManager.open(pluginData.assetPath())
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