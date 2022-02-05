package zlc.season.desolator

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import dalvik.system.DexClassLoader
import java.io.File
import java.lang.reflect.Array as JavaArray

object LoadUtils {
    var PLUGIN_PATH = ""

    fun init(context: Context) {
        val dir = context.getDir("plugins", AppCompatActivity.MODE_PRIVATE)
        PLUGIN_PATH = File(dir, "pluginb-release-unsigned.apk").path
    }

    fun load(context: Context) {
        try {
            //system filed
            val systemClassLoaderField = Class("dalvik.system.BaseDexClassLoader").field("pathList")
            val dexElementsField = Class("dalvik.system.DexPathList").field("dexElements")

            //get system elements
            val hostClassLoader = context.classLoader
            val hostPathList = systemClassLoaderField.of(hostClassLoader)
            val hostElements = dexElementsField.of(hostPathList) as Array<*>

            //get plugin elements
            val pluginClassLoader = DexClassLoader(
                PLUGIN_PATH,
                context.cacheDir.absolutePath,
                null,
                context.classLoader
            )
            val pluginPathList = systemClassLoaderField.of(pluginClassLoader)
            val pluginElements = dexElementsField.of(pluginPathList) as Array<*>

            //merge
            val newElements = JavaArray.newInstance(
                pluginElements.javaClass.componentType!!,
                hostElements.size + pluginElements.size
            ) as Array<*>

            System.arraycopy(hostElements, 0, newElements, 0, hostElements.size)
            System.arraycopy(pluginElements, 0, newElements, hostElements.size, pluginElements.size)

            //replace
            dexElementsField.set(hostPathList, newElements)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadAsset(context: Context): Resources? {
        try {
            //初始化一些成员变量和加载已安装的插件
            val packageInfoField = context.javaClass.field("mPackageInfo")
            val resourceField = context.javaClass.field("mResources")
            val packageInfo = packageInfoField.of(context)

            val assetManager = AssetManager::class.java.newInstance()
            val addAssetPathMethod = assetManager::class.java.getDeclaredMethod("addAssetPath", String::class.java)
            addAssetPathMethod.isAccessible = true
            addAssetPathMethod.invoke(assetManager, PLUGIN_PATH)
            val resource = Resources(assetManager, context.resources.displayMetrics, context.resources.configuration)

            resourceField.set(packageInfo, resource)

            return resource
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

}