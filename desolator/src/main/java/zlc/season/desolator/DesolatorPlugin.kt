package zlc.season.desolator

data class DesolatorPlugin(
    val pluginId: String,
    val apkPath: String,
    val classLoader: ClassLoader,
)