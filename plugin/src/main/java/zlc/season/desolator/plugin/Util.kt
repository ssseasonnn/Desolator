package zlc.season.desolator.plugin

import org.gradle.api.Project
import java.io.File
import java.util.*

const val PLUGIN_APP = "com.android.application"
const val PLUGIN_DF = "com.android.dynamic-feature"
const val PLUGIN_DESOLATOR = "io.github.ssseasonnn.desolator"

const val EXTENSION_NAME = "desolator"

const val DEST_DIR = "desolator_plugins"

const val GRADLE_GROUP = "desolator"

fun getTaskCopyToAppName(variantName: String): String {
    return "copy${variantName.cap()}ApkToApp"
}

fun getTaskCopyToProjectName(variantName: String): String {
    return "copy${variantName.cap()}ApkToProject"
}

fun getProjectApksDir(project: Project): File {
    return File(project.rootDir, DEST_DIR)
}

fun String.cap(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}