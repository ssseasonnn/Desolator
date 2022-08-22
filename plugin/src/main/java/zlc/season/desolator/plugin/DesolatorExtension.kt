package zlc.season.desolator.plugin

import org.gradle.api.provider.Property

abstract class DesolatorExtension {
    abstract fun getPluginId(): Property<String>
    abstract fun getPluginName(): Property<String>
    abstract fun getPluginVersion(): Property<String>
    abstract fun getEntrance(): Property<String>
    abstract fun getModuleName(): Property<String>
    abstract fun getDownloadUrl(): Property<String>
}