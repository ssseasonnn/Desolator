package zlc.season.desolatordemo

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import zlc.season.desolator.*


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
            Desolator.installPlugin(pluginData)
            Desolator.startPlugin(pluginData)
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