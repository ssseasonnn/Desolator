package zlc.season.desolator

data class DesolatorPlugin(
    val pluginId: Int,
    val apkPath: String,
    val classLoader: ClassLoader,
)