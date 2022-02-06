package zlc.season.desolator

import android.content.res.AssetManager
import android.content.res.Resources
import java.lang.reflect.Array.newInstance
import kotlin.Array as KotlinArray

object ClassManager {
    //system filed
    private val fieldPathList = Class("dalvik.system.BaseDexClassLoader").field("pathList")
    private val fieldDexElements = Class("dalvik.system.DexPathList").field("dexElements")

    private val classLoader = HookerInit.context.classLoader

    //origin info
    private val pathList = fieldPathList.of(classLoader)
    private val dexElements = fieldDexElements.of(pathList) as KotlinArray<*>

    //current dex elements
    private var currentDexElements = dexElements


    private val resourceField = HookerInit.contextImpl.javaClass.field("mResources")
    private val assetManager = AssetManager::class.java.newInstance()
    private val addAssetPathMethod = assetManager::class.java.method("addAssetPath", String::class)

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

    fun addPluginAsset(pluginInfo: PluginInfo) {
        try {
            addAssetPathMethod.invoke(assetManager, HookerInit.context.packageResourcePath)
            addAssetPathMethod.invoke(assetManager, pluginInfo.apkPath)

            val resource = Resources(assetManager, HookerInit.context.resources.displayMetrics, HookerInit.context.resources.configuration)

            resourceField.set(HookerInit.contextImpl, resource)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}