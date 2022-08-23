package zlc.season.desolator.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class DesolatorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        AppPluginConfig().configApp(project)
        DynamicFeaturePluginConfig().configFeature(project)
    }
}
