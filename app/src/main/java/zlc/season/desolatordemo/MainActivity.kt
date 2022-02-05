package zlc.season.desolatordemo

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import dalvik.system.DexClassLoader
import zlc.season.desolator.*
import java.io.File


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println(R.layout.activity_main)

        val btnHello = findViewById<Button>(R.id.btn_hello)
        btnHello.setOnClickListener {
//            startActivity(Intent(this, TestActivity::class.java))

//            val dir = getDir("plugins", MODE_PRIVATE)
//            val pluginFile = File(dir, "pluginb-release-unsigned.apk")
//            pluginFile.unzip(dir.path)
//
//            val libPath = dir.path + "/classes.dex"
//            val tmpDir = getDir("dex", MODE_PRIVATE)
//            val classloader =
//                DexClassLoader(libPath, tmpDir.absolutePath, null, this.javaClass.classLoader)
//            val classToLoad =
//                classloader.loadClass("zlc.season.pluginb.BPluginActivity") as Class<Any>
//
//            startActivity(Intent(this, classToLoad))

            val pluginData = PluginData(1, "a", 1)
            PluginManager.installPlugin(pluginData)
            PluginManager.startPlugin(pluginData)
        }
    }

    fun test() {
        val field = Class("android.app.ActivityThread").field("sPackageManager")
        val obj = field.get(null)
        val method = Class("android.app.ActivityThread").method("getPackageManager")
        obj.hashCode()
    }

//    fun install(apkPath: String, context: Context): Boolean {
//        Log.d("TAG", "Installing apk at $apkPath")
//        return try {
//            val apkUri: Uri = Uri.fromFile(File(apkPath))
//            val installerPackageName = "MyInstaller"
//            context.packageManager.installPackage(
//                apkUri,
//                installObserver,
//                PackageManager.INSTALL_REPLACE_EXISTING,
//                installerPackageName
//            )
//            true
//        } catch (e: Exception) {
//            e.printStackTrace()
//            false
//        }
//    }
}