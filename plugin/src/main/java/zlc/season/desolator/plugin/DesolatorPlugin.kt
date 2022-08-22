package zlc.season.desolator.plugin

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.google.gson.Gson
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.util.*

class DesolatorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.withPlugin("com.android.dynamic-feature") {
            val appDir = getAppDir(project)
            if (appDir == null) {
                println("Desolator --> App project not found!")
                return@withPlugin
            }

            val extension = project.extensions.create("desolator", DesolatorExtension::class.java)

            val androidExtension =
                project.extensions.getByType(AndroidComponentsExtension::class.java)
            androidExtension.onVariants { variant ->
                val pluginData = createPluginData(extension, project)
                configCopyApkToAppTask(project, variant, pluginData, appDir)
                configCopyApkToProjectTask(project, variant, pluginData, appDir)
            }
        }
    }

    private fun configCopyApkToAppTask(
        project: Project,
        variant: Variant,
        pluginData: PluginData,
        appDir: File
    ) {
        val copyApksProvider = project.tasks.register(
            "copy${variant.name.cap()}ApkToApp",
            CopyApksTask::class.java
        )

        val transformationRequest = variant.artifacts.use(copyApksProvider)
            .wiredWithDirectories(
                CopyApksTask::apkFolder,
                CopyApksTask::outFolder
            )
            .toTransformMany(SingleArtifact.APK)

        copyApksProvider.configure {
            it.pluginFileName.set(pluginData.pluginFileName())
            it.pluginJsonContent.set(createPluginInfoJson(pluginData))
            it.pluginJsonFileName.set(pluginData.pluginJsonFileName())
            it.destDir.set(getAppAssetDir(appDir, pluginData.name))
            it.transformationRequest.set(transformationRequest)
        }
    }

    private fun configCopyApkToProjectTask(
        project: Project,
        variant: Variant,
        pluginData: PluginData,
        appDir: File
    ) {
        val copyApksProvider = project.tasks.register(
            "copy${variant.name.cap()}ApkToProject",
            CopyApksTask::class.java
        )

        val transformationRequest = variant.artifacts.use(copyApksProvider)
            .wiredWithDirectories(
                CopyApksTask::apkFolder,
                CopyApksTask::outFolder
            )
            .toTransformMany(SingleArtifact.APK)

        copyApksProvider.configure {
            it.pluginFileName.set(pluginData.pluginFileName())
            it.pluginJsonContent.set(createPluginInfoJson(pluginData))
            it.pluginJsonFileName.set(pluginData.pluginJsonFileName())
            it.destDir.set(getProjectApksDir(appDir, pluginData.name))
            it.transformationRequest.set(transformationRequest)
        }
    }

    private fun getAppDir(project: Project): File? {
        var appDir: File? = null
        project.rootProject.allprojects { each ->
            if (each.pluginManager.hasPlugin("com.android.application")) {
                appDir = each.projectDir
            }
        }
        return appDir
    }

    private fun String.cap(): String {
        return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    private fun getAppAssetDir(appDir: File, pluginName: String): File {
        val fs = File.separator
        return File(
            appDir,
            "src${fs}main${fs}assets${fs}desolator_plugins${fs}plugin_${pluginName}"
        )
    }

    private fun getProjectApksDir(appDir: File, pluginName: String): File {
        val fs = File.separator
        return File(appDir.parent, "desolator_plugins${fs}plugin_${pluginName}")
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
