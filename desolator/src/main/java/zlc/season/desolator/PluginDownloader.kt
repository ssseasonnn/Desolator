package zlc.season.desolator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import zlc.season.desolator.util.pluginDir
import zlc.season.desolator.util.pluginFileName
import zlc.season.downloadx.State
import zlc.season.downloadx.download

interface PluginDownloader {
    fun startDownload(pluginData: PluginData): Flow<State>

    fun cancelDownload(pluginData: PluginData)
}

class DefaultPluginDownloader(
    private val coroutineScope: CoroutineScope
) : PluginDownloader {
    override fun startDownload(pluginData: PluginData): Flow<State> {
        val task = coroutineScope.download(
            pluginData.downloadUrl,
            pluginData.pluginFileName(),
            pluginData.pluginDir().path
        )
        task.start()
        return task.state()
    }

    override fun cancelDownload(pluginData: PluginData) {
        val task = coroutineScope.download(pluginData.downloadUrl)
        task.stop()
    }
}