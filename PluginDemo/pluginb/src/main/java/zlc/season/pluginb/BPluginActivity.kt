package zlc.season.pluginb

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import zlc.season.pluginb.AssertHook.getPluginResources
import zlc.season.pluginb.AssertHook.getResId


class BPluginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plugin_b)

//        val dir = getDir("plugins", MODE_PRIVATE)
////            val pluginFile = File(dir, "classes.dex")
////            pluginFile.unzip(dir.path)
//        val libPath = dir.path + "/classes.dex"
//
//        val resId = getResId(this.application, libPath, "zlc.season.pluginb", "activity_plugin_b")
//        val resources = getPluginResources(this.application, dir.path+"/pluginb-release-unsigned.apk")
//        val layout = resources.getLayout(resId)
//        val view = layoutInflater.inflate(layout,null)
//        setContentView(view)
    }
}