package zlc.season.desolator

import android.content.Intent
import zlc.season.desolator.DesolatorInit.Companion.classLoader
import zlc.season.desolator.DesolatorInit.Companion.context

object Desolator {
    private val apkController by lazy { ApkController() }
    private val pluginController by lazy { PluginController() }

    fun installInternalPlugin() {
        val pluginList = apkController.initInternalPlugin()
        pluginList.forEach {
            pluginController.addPlugin(it)
        }
    }

    fun installPlugin(pluginData: PluginData) {
        val pluginInfo = apkController.createPlugin(pluginData)
        pluginInfo?.let {
            pluginController.addPlugin(it)
        }
    }

    fun startPlugin(pluginData: PluginData) {
        if (pluginController.isPluginLoaded(pluginData.id)) {
            val activityCls = classLoader.loadClass("com.example.plugina.PluginActivity") as Class<*>
            val intent = Intent(context, activityCls)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } else {
            "$pluginData has not install!".loge()
        }
    }

    fun startPlugin(pluginId: Int) {
        if (pluginController.isPluginLoaded(pluginId)) {
            val activityCls = classLoader.loadClass("com.example.plugina.PluginActivity") as Class<*>
            val intent = Intent(context, activityCls)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } else {
            "Plugin: $pluginId has not install!".loge()
        }
    }
}