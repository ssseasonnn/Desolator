package zlc.season.desolator

import android.content.res.AssetManager
import android.content.res.Resources
import zlc.season.desolator.DesolatorInit.Companion.classLoader
import zlc.season.desolator.DesolatorInit.Companion.context
import zlc.season.desolator.DesolatorInit.Companion.contextImpl
import java.lang.reflect.Array.newInstance
import kotlin.Array as KotlinArray

class ApkManager {
    private val assetController by lazy { AssetController() }
    private val dexController by lazy { DexController() }

    private val pluginMap = mutableMapOf<Int, PluginInfo>()

    fun addPlugin(pluginInfo: PluginInfo) {
        dexController.addPluginDex(pluginInfo)
        assetController.addPluginAsset(pluginInfo)

        pluginMap[pluginInfo.pluginId] = pluginInfo
    }

    fun isPluginLoaded(pluginId: Int): Boolean {
        return pluginMap[pluginId] != null
    }
}

class AssetController {
    private val resourceField = contextImpl.javaClass.field("mResources")
    private val assetManager = AssetManager::class.java.newInstance()
    private val addAssetPathMethod = assetManager::class.java.method("addAssetPath", String::class)

    init {
        addAssetPathMethod.invoke(assetManager, context.packageResourcePath)
    }

    fun addPluginAsset(pluginInfo: PluginInfo) {
        try {
            addAssetPathMethod.invoke(assetManager, pluginInfo.apkPath)
            val resource = Resources(assetManager, context.resources.displayMetrics, context.resources.configuration)
            resourceField.set(contextImpl, resource)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class DexController {
    //system filed
    private val fieldPathList = Class("dalvik.system.BaseDexClassLoader").field("pathList")
    private val fieldDexElements = Class("dalvik.system.DexPathList").field("dexElements")

    //origin info
    private val pathList = fieldPathList.of(classLoader)
    private val dexElements = fieldDexElements.of(pathList) as KotlinArray<*>

    //current dex elements
    private var currentDexElements = dexElements

    fun addPluginDex(pluginInfo: PluginInfo) {
        try {
            val pluginPathList = fieldPathList.of(pluginInfo.classLoader)
            val pluginDexElements = fieldDexElements.of(pluginPathList) as KotlinArray<*>

            val mergedDexElements = newDexElementsArray(pluginDexElements)

            //replace
            fieldDexElements.set(pathList, mergedDexElements)

            //save current
            currentDexElements = mergedDexElements
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun newDexElementsArray(pluginDexElements: KotlinArray<*>): KotlinArray<*> {
        val result = newInstance(dexElements.javaClass.componentType!!, currentDexElements.size + pluginDexElements.size) as KotlinArray<*>
        System.arraycopy(currentDexElements, 0, result, 0, currentDexElements.size)
        System.arraycopy(pluginDexElements, 0, result, currentDexElements.size, pluginDexElements.size)
        return result
    }
}
