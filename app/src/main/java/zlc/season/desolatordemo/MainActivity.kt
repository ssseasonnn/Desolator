package zlc.season.desolatordemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import java.io.File
import dalvik.system.DexClassLoader


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnHello = findViewById<Button>(R.id.btn_hello)
        btnHello.setOnClickListener {
//            startActivity(Intent(this, TestActivity::class.java))

            val dir = getDir("plugins", MODE_PRIVATE)
            val pluginFile = File(dir, "pluginb-release-unsigned.apk")
            pluginFile.unzip(dir.path)

            val libPath = dir.path + "/classes.dex"
            val tmpDir = getDir("dex", MODE_PRIVATE)
            val classloader = DexClassLoader(libPath, tmpDir.absolutePath, null, this.javaClass.classLoader)
            val classToLoad = classloader.loadClass("zlc.season.pluginb.BPluginActivity") as Class<Any>

            startActivity(Intent(this, classToLoad))
//            val myInstance = classToLoad.newInstance()
//            val doSomething = classToLoad.getMethod("doSomething")

//            doSomething.invoke(myInstance)


        }
    }
}