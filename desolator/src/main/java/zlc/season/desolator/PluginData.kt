package zlc.season.desolator

data class PluginData(
    val id: Int,
    val name: String,
    val version: Int
) {
    override fun toString(): String {
        return "Plugin: [id = $id, name = $name, version = $version]"
    }
}