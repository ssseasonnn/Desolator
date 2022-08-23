package zlc.season.desolator.plugin

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.google.gson.Gson
import org.gradle.api.Project
import java.io.File

class DynamicFeaturePluginConfig {
    fun configFeature(project: Project) {
        project.pluginManager.withPlugin(PLUGIN_DF) {
            val extension =
                project.extensions.create(EXTENSION_NAME, DesolatorExtension::class.java)
            val androidExtension =
                project.extensions.getByType(AndroidComponentsExtension::class.java)
            androidExtension.onVariants { variant ->
                val pluginData = createPluginData(extension, project)
                configCopyApkToProjectTask(project, variant, pluginData)
            }
        }
    }

    private fun configCopyApkToProjectTask(
        project: Project,
        variant: Variant,
        pluginData: PluginData
    ) {
        val copyApksProvider = project.tasks.register(
            getTaskCopyToProjectName(variant.name),
            CopyApksTask::class.java
        )

        val transformationRequest = variant.artifacts.use(copyApksProvider)
            .wiredWithDirectories(
                CopyApksTask::apkFolder,
                CopyApksTask::outFolder
            )
            .toTransformMany(SingleArtifact.APK)

        copyApksProvider.configure {
            it.group = GRADLE_GROUP
            it.pluginFileName.set(pluginData.pluginFileName())
            it.pluginJsonContent.set(createPluginInfoJson(pluginData))
            it.pluginJsonFileName.set(pluginData.pluginJsonFileName())
            it.destDir.set(getProjectApkDirForPlugin(project, pluginData.name))
            it.transformationRequest.set(transformationRequest)
        }
    }

    private fun getProjectApkDirForPlugin(project: Project, pluginName: String): File {
        return File(getProjectApksDir(project), "plugin_${pluginName}")
    }

    private fun createPluginInfoJson(pluginData: PluginData): String {
        return Gson().toJson(pluginData)
    }

    private fun createPluginData(
        extension: DesolatorExtension,
        project: Project
    ): PluginData {
        val id = extension.getPluginId().getOrElse(project.name.hashCode().toString())
        val name = extension.getPluginName().getOrElse(project.name)
        val version = extension.getPluginVersion().getOrElse("1")
        val entrance = extension.getEntrance().getOrElse("")
        val moduleName = extension.getModuleName().getOrElse("")
        val downloadUrl = extension.getDownloadUrl().getOrElse("")
        return PluginData(id, name, version, entrance, moduleName, downloadUrl)
    }
}