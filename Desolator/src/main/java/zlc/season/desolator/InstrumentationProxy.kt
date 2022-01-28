package zlc.season.desolator

import android.app.Instrumentation
import android.content.pm.PackageManager
import android.os.Bundle

import android.content.Intent

import android.app.Activity
import android.content.Context

import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import dalvik.system.DexClassLoader

class InstrumentationProxy(private val realContext: Context, private val real: Instrumentation, private val packageManager: PackageManager) :
    Instrumentation() {

    fun execStartActivity(
        who: Context, contextThread: IBinder?, token: IBinder?, target: Activity?,
        intent: Intent, requestCode: Int, options: Bundle?
    ): ActivityResult? {
        val infos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        if (infos.isNullOrEmpty()) {
            intent.putExtra("target", intent.component!!.className)
            intent.setClassName(who, "zlc.season.desolator.StubActivity")
        }

        try {
            val method = Instrumentation::class.java.getDeclaredMethod(
                "execStartActivity", Context::class.java, IBinder::class.java, IBinder::class.java, Activity::class.java,
                Intent::class.java, Int::class.java, Bundle::class.java
            )
            method.isAccessible = true
            return method.invoke(real, who, contextThread, token, target, intent, requestCode, options) as? ActivityResult
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun newActivity(
        cl: ClassLoader, className: String,
        intent: Intent
    ): Activity {
        val intentName = intent.getStringExtra("target")
        val realName = if (!intentName.isNullOrEmpty()) {
            intentName
        } else {
            className
        }
        val classLoader = if (!intentName.isNullOrEmpty()) {
            val dir = realContext.getDir("plugins", AppCompatActivity.MODE_PRIVATE)
//            val pluginFile = File(dir, "classes.dex")
//            pluginFile.unzip(dir.path)

            val libPath = dir.path + "/classes.dex"
            val tmpDir = realContext.getDir("dex", AppCompatActivity.MODE_PRIVATE)
            val classloader = DexClassLoader(libPath, tmpDir.absolutePath, null, this.javaClass.classLoader)
//        val classToLoad = classloader.loadClass("zlc.season.pluginb.BPluginActivity") as Class<Any>
            classloader
        } else {
            cl

        }

        return real.newActivity(classLoader, realName, intent)
    }
}
