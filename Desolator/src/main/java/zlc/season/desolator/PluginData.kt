package zlc.season.desolator

data class PluginData(
    val id: Int,
    val name: String,
    val version: Int
) {
    fun fileName(): String {
//        return "plugin_${name}_${id}_${version}.apk"
        return "plugina-debug.apk"
    }
}