package zlc.season.desolator

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import dalvik.system.DexClassLoader
import java.io.File

object PluginManager {
    val PLUGIN_PATH = HookerInit.context.getDir("plugins", Context.MODE_PRIVATE)


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