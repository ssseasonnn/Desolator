package zlc.season.desolator

data class PluginData(
    val id: String,
    val name: String,
    val version: String,
    val entrance: String = "",
    val moduleName: String = "",
    val downloadUrl: String = ""
) {
    override fun toString(): String {
        return "Plugin: [id = $id, name = $name, version = $version]"
    }
}