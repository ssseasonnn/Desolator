package zlc.season.desolator.plugin

import com.android.build.api.artifact.ArtifactTransformationRequest
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.BuiltArtifact
import com.android.build.api.variant.BuiltArtifactsLoader
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.DefaultProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import java.io.File
import java.io.Serializable
import java.util.*
import javax.inject.Inject

class DesolatorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.withPlugin("com.android.dynamic-feature") {
            var appDir: File? = null
            project.rootProject.allprojects { each ->
                if (each.pluginManager.hasPlugin("com.android.application")) {
                    appDir = each.projectDir
                }
            }

            if (appDir == null) {
                println("Desolator --> App project not found!")
                return@withPlugin
            }

            val extension = project.extensions.create("desolator", DesolatorExtension::class.java)

            val androidComponentsExtension = project.extensions.getByType(AndroidComponentsExtension::class.java)
            androidComponentsExtension.onVariants { variant ->
                val name = extension.getPluginName().getOrElse(project.name)
                val id = extension.getPluginId().getOrElse(project.name.hashCode())
                val version = extension.getPluginVersion().getOrElse(1)
                val pluginData = PluginData(name, id, version)

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
                    it.appAssetDir.set(getAppAssetDir(appDir!!))
                    it.transformationRequest.set(transformationRequest)
                }
            }
        }
    }

    private fun String.cap(): String {
        return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    private fun getAppAssetDir(appDir: File): File {
        val fs = File.separator
        return File(appDir, "src${fs}main${fs}assets${fs}desolator_plugins")
    }

    data class PluginData(val name: String, val id: Int, val version: Int) {
        fun pluginFileName(): String {
            return "${name}_${id}_${version}.apk"
        }
    }
}

abstract class DesolatorExtension {
    abstract fun getPluginName(): Property<String>
    abstract fun getPluginId(): Property<Int>
    abstract fun getPluginVersion(): Property<Int>
}

interface WorkItemParameters : WorkParameters, Serializable {
    val inputApkFile: RegularFileProperty
    val outputApkFile: RegularFileProperty
}

abstract class WorkItem @Inject constructor(private val workItemParameters: WorkItemParameters) : WorkAction<WorkItemParameters> {
    override fun execute() {
        val file = workItemParameters.inputApkFile.asFile.get()
        println("input file $file")
        val ofile = workItemParameters.outputApkFile.get().asFile
        println("output file $ofile")

        workItemParameters.outputApkFile.get().asFile.delete()
        workItemParameters.inputApkFile.asFile.get().copyTo(
            workItemParameters.outputApkFile.get().asFile
        )
    }
}

abstract class CopyApksTask @Inject constructor(private val workers: WorkerExecutor) : DefaultTask() {

    @get:InputFiles
    abstract val apkFolder: DirectoryProperty

    @get:OutputDirectory
    abstract val outFolder: DirectoryProperty

    @get:Internal
    abstract val appAssetDir: Property<File>

    @get:Internal
    abstract val pluginFileName: Property<String>

    @get:Internal
    abstract val transformationRequest: Property<ArtifactTransformationRequest<CopyApksTask>>

    @TaskAction
    fun taskAction() {
        transformationRequest.get().submit(this, workers.noIsolation(), WorkItem::class.java)
        { builtArtifact: BuiltArtifact,
          outputLocation: Directory,
          param: WorkItemParameters ->
            val inputFile = File(builtArtifact.outputFile)
            param.inputApkFile.set(inputFile)

            val outputFile = File(appAssetDir.get(), pluginFileName.get())
            param.outputApkFile.set(outputFile)
            param.outputApkFile.get().asFile
        }
    }
}