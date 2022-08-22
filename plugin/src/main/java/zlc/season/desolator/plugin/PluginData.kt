package zlc.season.desolator.plugin

data class PluginData(
    val id: String,
    val name: String,
    val version: String,
    val entrance: String,
    val moduleName: String,
    val downloadUrl: String
) {
    fun pluginFileName(): String {
        return "${name}.apk"
    }

    fun pluginJsonFileName(): String {
        return "plugin_info.json"
    }
}