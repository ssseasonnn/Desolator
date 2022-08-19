package zlc.season.desolator

import android.content.res.AssetManager
import android.content.res.Resources
import zlc.season.desolator.DesolatorInit.Companion.classLoader
import zlc.season.desolator.DesolatorInit.Companion.context
import zlc.season.desolator.DesolatorInit.Companion.contextImpl
import zlc.season.desolator.util.Class
import zlc.season.desolator.util.field
import zlc.season.desolator.util.method
import zlc.season.desolator.util.of
import java.lang.reflect.Array.newInstance
import kotlin.Array as KotlinArray

class PluginLoader {
    private val resourceLoader by lazy { ResourceLoader() }
    private val dexLoader by lazy { DexLoader() }

    private val pluginMap = mutableMapOf<Int, DesolatorPlugin>()


    fun loadPlugin(plugin: DesolatorPlugin) {
        dexLoader.addPlugin(plugin)
        resourceLoader.addPlugin(plugin)

        pluginMap[plugin.pluginId] = plugin
    }

    fun isPluginLoaded(pluginId: Int): Boolean {
        return pluginMap[pluginId] != null
    }

    class ResourceLoader {
        private val resourceField = contextImpl.javaClass.field("mResources")
        private val assetManager = AssetManager::class.java.newInstance()
        private val addAssetPathMethod =
            assetManager::class.java.method("addAssetPath", String::class)

        init {
            addAssetPathMethod.invoke(assetManager, context.packageResourcePath)
        }

        fun addPlugin(desolatorPlugin: DesolatorPlugin) {
            try {
                addAssetPathMethod.invoke(assetManager, desolatorPlugin.apkPath)
                val resource = Resources(
                    assetManager,
                    context.resources.displayMetrics,
                    context.resources.configuration
                )
                resourceField.set(contextImpl, resource)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    class DexLoader {
        //system filed
        private val fieldPathList = Class("dalvik.system.BaseDexClassLoader").field("pathList")
        private val fieldDexElements = Class("dalvik.system.DexPathList").field("dexElements")

        //origin info
        private val pathList = fieldPathList.of(classLoader)
        private val dexElements = fieldDexElements.of(pathList) as KotlinArray<*>

        //current dex elements
        private var currentDexElements = dexElements

        fun addPlugin(plugin: DesolatorPlugin) {
            try {
                val pluginPathList = fieldPathList.of(plugin.classLoader)
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
            val result = newInstance(
                dexElements.javaClass.componentType!!,
                currentDexElements.size + pluginDexElements.size
            ) as KotlinArray<*>
            System.arraycopy(currentDexElements, 0, result, 0, currentDexElements.size)
            System.arraycopy(
                pluginDexElements,
                0,
                result,
                currentDexElements.size,
                pluginDexElements.size
            )
            return result
        }
    }
}


