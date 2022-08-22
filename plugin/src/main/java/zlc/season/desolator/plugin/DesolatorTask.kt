package zlc.season.desolator.plugin

import com.android.build.api.artifact.ArtifactTransformationRequest
import com.android.build.api.variant.BuiltArtifact
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
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
import javax.inject.Inject

interface CopyApkParameters : WorkParameters, Serializable {
    val inputApkFile: RegularFileProperty
    val outputApkFile: RegularFileProperty
}

abstract class CopyApkWork @Inject constructor(
    private val copyApkParameters: CopyApkParameters
) : WorkAction<CopyApkParameters> {
    override fun execute() {
        val inputFile = copyApkParameters.inputApkFile.asFile.get()
        val outputFile = copyApkParameters.outputApkFile.get().asFile

        outputFile.delete()
        inputFile.copyTo(outputFile)
    }
}

interface CreateJsonParameters : WorkParameters, Serializable {
    val jsonContent: Property<String>
    val outputJsonFile: RegularFileProperty
}

abstract class CreateJsonWork @Inject constructor(private val parameters: CreateJsonParameters) :
    WorkAction<CreateJsonParameters> {
    override fun execute() {
        val file = parameters.outputJsonFile.get().asFile
        file.writeText(parameters.jsonContent.get())
    }
}

abstract class CopyApksTask @Inject constructor(
    private val workers: WorkerExecutor
) : DefaultTask() {

    @get:InputFiles
    abstract val apkFolder: DirectoryProperty

    @get:OutputDirectory
    abstract val outFolder: DirectoryProperty

    @get:Internal
    abstract val destDir: Property<File>

    @get:Internal
    abstract val pluginFileName: Property<String>

    @get:Internal
    abstract val pluginJsonContent: Property<String>

    @get:Internal
    abstract val pluginJsonFileName: Property<String>

    @get:Internal
    abstract val transformationRequest: Property<ArtifactTransformationRequest<CopyApksTask>>

    @TaskAction
    fun taskAction() {
        transformationRequest.get().submit(
            this,
            workers.noIsolation(),
            CopyApkWork::class.java
        ) { builtArtifact: BuiltArtifact,
            _: Directory,
            param: CopyApkParameters ->
            val inputFile = File(builtArtifact.outputFile)
            param.inputApkFile.set(inputFile)

            val outputFile = File(destDir.get(), pluginFileName.get())
            param.outputApkFile.set(outputFile)
            param.outputApkFile.get().asFile
        }

        transformationRequest.get().submit(
            this,
            workers.noIsolation(),
            CreateJsonWork::class.java
        ) { _: BuiltArtifact, _: Directory, parameters: CreateJsonParameters ->
            parameters.jsonContent.set(pluginJsonContent.get())
            val outputFile = File(destDir.get(), pluginJsonFileName.get())
            parameters.outputJsonFile.set(outputFile)
            parameters.outputJsonFile.get().asFile
        }
    }
}


