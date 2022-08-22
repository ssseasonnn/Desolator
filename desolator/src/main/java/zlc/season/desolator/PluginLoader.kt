package zlc.season.desolator

import android.app.Activity
import android.content.res.AssetManager
import zlc.season.desolator.DesolatorHelper.assetManager
import zlc.season.desolator.util.*
import java.lang.reflect.Array.newInstance
import kotlin.Array as KotlinArray

class PluginLoader {
    private val resourceLoader by lazy { ResourceLoader() }
    private val dexLoader by lazy { DexLoader() }

    private val pluginMap = mutableMapOf<String, DesolatorPlugin>()


    fun loadPlugin(plugin: DesolatorPlugin) {
        dexLoader.addPlugin(plugin)
        resourceLoader.addPlugin(plugin)

        pluginMap[plugin.pluginId] = plugin
    }

    fun loadPluginInternal(plugin: DesolatorPlugin, activity: Activity) {
        dexLoader.addPlugin(plugin)
        resourceLoader.addPluginInternal(plugin, activity)

        pluginMap[plugin.pluginId] = plugin
    }

    fun isPluginLoaded(pluginId: String): Boolean {
        return pluginMap[pluginId] != null
    }

    class ResourceLoader {
        private val assetManagerCls = AssetManager::class.java
        private val addAssetPathMethod = assetManagerCls.method("addAssetPath", String::class)

        fun addPlugin(desolatorPlugin: DesolatorPlugin) {
            try {
                addAssetPathMethod.invoke(assetManager, desolatorPlugin.apkPath)
            } catch (e: Exception) {
                e.logw()
            }
        }

        fun addPluginInternal(desolatorPlugin: DesolatorPlugin, activity: Activity) {
            try {
                addAssetPathMethod.invoke(assetManager, desolatorPlugin.apkPath)
                addAssetPathMethod.invoke(activity.assets, desolatorPlugin.apkPath)
            } catch (e: Exception) {
                e.logw()
            }
        }
    }

    class DexLoader {
        //system filed
        private val fieldPathList = Class("dalvik.system.BaseDexClassLoader").field("pathList")
        private val fieldDexElements = Class("dalvik.system.DexPathList").field("dexElements")

        //origin info
        private val pathList = fieldPathList.of(DesolatorHelper.classLoader)
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
                e.logw()
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


