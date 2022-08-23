package zlc.season.desolator.plugin

import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Project
import java.io.File

class AppPluginConfig {
    fun configApp(project: Project) {
        project.pluginManager.withPlugin(PLUGIN_APP) {
            val androidExtension =
                project.extensions.getByType(AndroidComponentsExtension::class.java)
            androidExtension.onVariants { variant ->
                project.tasks.create(getTaskCopyToAppName(variant.name)) { task ->
                    task.group = GRADLE_GROUP

                    task.doLast {
                        val appAssetDir = getAppAssetDir(project)
                        val apksDir = getProjectApksDir(project)
                        if (apksDir.exists() && apksDir.isDirectory) {
                            appAssetDir.deleteRecursively()
                            apksDir.copyRecursively(appAssetDir)
                        }
                    }
                }
            }
        }
    }

    private fun getAppAssetDir(project: Project): File {
        val fs = File.separator
        return File(project.projectDir, "src${fs}main${fs}assets${fs}${DEST_DIR}")
    }
}