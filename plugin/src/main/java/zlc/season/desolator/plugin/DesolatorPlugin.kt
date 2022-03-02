package zlc.season.desolator.plugin

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.BuiltArtifactsLoader
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class DesolatorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val androidComponentsExtension = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponentsExtension.onVariants { variant ->
            project.tasks.register("${variant.name}CopyApks", PluginApkTask::class.java) {
                it.apkFolder.set(variant.artifacts.get(SingleArtifact.APK))
                it.builtArtifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
            }
        }
    }
}

abstract class PluginApkTask : DefaultTask() {
    @get:InputFiles
    abstract val apkFolder: DirectoryProperty

    @get:Internal
    abstract val builtArtifactsLoader: Property<BuiltArtifactsLoader>

    @TaskAction
    fun taskAction() {
        val builtArtifacts = builtArtifactsLoader.get().load(apkFolder.get()) ?: throw RuntimeException("Cannot load APKs")
        builtArtifacts.elements.forEach {
            println("Got an APK at ${it.outputFile}")
        }
    }
}