package zlc.season.desolator

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.AssetManager
import android.content.res.Resources
import dalvik.system.DexClassLoader
import java.io.File


object PluginManager {
    val PLUGIN_DIR = HookerInit.context.getDir("plugins", Context.MODE_PRIVATE)
    val PLUGIN_DEX_DIR = HookerInit.context.getDir("dex", Context.MODE_PRIVATE)

    val pluginMap = mutableMapOf<String, PluginInfo>()

    fun installPlugin(pluginData: PluginData) {
        val pluginFileName = pluginData.fileName()
        val pluginFile = File(PLUGIN_DIR, pluginFileName)
        val classLoader = DexClassLoader(
            pluginFile.path,
            PLUGIN_DEX_DIR.absolutePath,
            null,
            Thread.currentThread().contextClassLoader
        )
        val pluginInfo = PluginInfo(pluginFile.path, classLoader)
        pluginMap[pluginData.fileName()] = pluginInfo
        load(classLoader)
        loadAsset(HookerInit.context, pluginFile.path)
    }

    fun load(pluginClassLoader: DexClassLoader) {
        try {
            //system filed
            val systemClassLoaderField = Class("dalvik.system.BaseDexClassLoader").field("pathList")
            val dexElementsField = Class("dalvik.system.DexPathList").field("dexElements")

            //get system elements
            val hostClassLoader = HookerInit.context.classLoader
            val hostPathList = systemClassLoaderField.of(hostClassLoader)
            val hostElements = dexElementsField.of(hostPathList) as Array<*>

            val pluginPathList = systemClassLoaderField.of(pluginClassLoader)
            val pluginElements = dexElementsField.of(pluginPathList) as Array<*>

            //merge
            val newElements = java.lang.reflect.Array.newInstance(
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

    fun loadAsset(context: Context, path: String): Resources? {
        try {
            //初始化一些成员变量和加载已安装的插件
            val contextImpl = (HookerInit.context as Application).baseContext
            val javaClass = contextImpl.javaClass
            val packageInfoField = javaClass.field("mPackageInfo")
            val resourceField = javaClass.field("mResources")
            val packageInfo = packageInfoField.of(contextImpl)
            val resourceobj = resourceField.of(contextImpl)

            val assetManager = AssetManager::class.java.newInstance()
            val addAssetPathMethod =
                assetManager::class.java.getDeclaredMethod("addAssetPath", String::class.java)
            addAssetPathMethod.isAccessible = true
            addAssetPathMethod.invoke(assetManager, path)
            val resource = Resources(
                assetManager,
                context.resources.displayMetrics,
                context.resources.configuration
            )

            resourceField.set(contextImpl, resource)

            return resource
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


    fun startPlugin(pluginData: PluginData) {
        val pluginFileName = pluginData.fileName()
        val pluginInfo = pluginMap[pluginFileName] ?: return

        val activityCls =
            Thread.currentThread().contextClassLoader.loadClass("com.example.plugina.PluginActivity") as Class<Any>
        val intent = Intent(HookerInit.context, activityCls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        HookerInit.context.startActivity(intent)
    }

//    fun test() {
//        val dir = getDir("plugins", AppCompatActivity.MODE_PRIVATE)
//        val pluginFile = File(dir, "pluginb-release-unsigned.apk")
//        pluginFile.unzip(dir.path)
//
//        val libPath = dir.path + "/classes.dex"
//        val tmpDir = getDir("dex", AppCompatActivity.MODE_PRIVATE)
//        val classloader = DexClassLoader(libPath, tmpDir.absolutePath, null, this.javaClass.classLoader)
//        val classToLoad = classloader.loadClass("zlc.season.pluginb.BPluginActivity") as Class<Any>
//    }
}