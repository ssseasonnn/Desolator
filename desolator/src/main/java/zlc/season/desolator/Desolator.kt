package zlc.season.desolator

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
}