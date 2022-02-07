package zlc.season.desolator

data class PluginInfo(
    val pluginId: Int,
    val apkPath: String,
    val classLoader: ClassLoader,
)