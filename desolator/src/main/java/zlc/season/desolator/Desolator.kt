package zlc.season.desolator

import androidx.fragment.app.Fragment
import zlc.season.desolator.DesolatorInit.classLoader
import zlc.season.desolator.util.logw

object Desolator {
    private val pluginManager by lazy { PluginManager() }
    private val pluginLoader by lazy { PluginLoader() }

    fun installInternalPlugin() {
        val pluginList = pluginManager.initInternalPlugin()
        pluginList.forEach {
            pluginLoader.loadPlugin(it)
        }
    }

    fun installPlugin(pluginData: PluginData) {
        val pluginInfo = pluginManager.createPlugin(pluginData)
        pluginInfo?.let {
            pluginLoader.loadPlugin(it)
        }
    }

    fun startPlugin(pluginData: PluginData) {
        if (!pluginLoader.isPluginLoaded(pluginData.id)) {
            "[$pluginData] not install!".logw()
            return
        }

        val fragmentCls = classLoader.loadClass(pluginData.entrance) as Class<*>
        if (Fragment::class.java.isAssignableFrom(fragmentCls)) {
            val fragment = fragmentCls.newInstance() as Fragment
            DesolatorInit.activity?.apply {
                supportFragmentManager.beginTransaction().apply {
                    add(android.R.id.content, fragment)
                    commit()
                }
            }
        }
    }
}