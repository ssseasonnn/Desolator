package zlc.season.desolator

import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import zlc.season.desolator.DesolatorInit.classLoader
import zlc.season.desolator.util.logw
import zlc.season.downloadx.Progress
import zlc.season.downloadx.State

object Desolator {
    private val pluginManager by lazy { PluginManager() }
    private val pluginLoader by lazy { PluginLoader() }

    private val pluginDownloader by lazy { DefaultPluginDownloader() }
    private val coroutineScope by lazy { GlobalScope }

    fun installInternalPlugin(): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val pluginList = pluginManager.initInternalPlugin()
            pluginList.forEach {
                pluginLoader.loadPlugin(it)
            }
        }
    }

    fun installPlugin(
        pluginData: PluginData,
        onProgress: (Progress) -> Unit,
        onSuccess: () -> Unit,
        onFailed: () -> Unit
    ): Job {
        val flow = pluginDownloader.startDownload(pluginData)
        val job = flow.onEach {
            when (it) {
                is State.Downloading -> {
                    onProgress(it.progress)
                }
                is State.Failed, is State.Stopped -> {
                    onFailed()
                }
                is State.Succeed -> {
                    realInstallPlugin(pluginData)
                    onSuccess()
                }
                else -> {}
            }
        }.launchIn(coroutineScope)

        job.invokeOnCompletion {
            pluginDownloader.cancelDownload(pluginData)
        }
        return job
    }

    private suspend fun realInstallPlugin(pluginData: PluginData) {
        withContext(Dispatchers.IO) {
            val pluginInfo = pluginManager.createPlugin(pluginData)
            pluginInfo?.let {
                pluginLoader.loadPlugin(it)
            }
        }
    }

    fun startPlugin(pluginData: PluginData) {
        if (!pluginLoader.isPluginLoaded(pluginData.id)) {
            "[$pluginData] not install!".logw()
            return
        }

        val fragmentCls = classLoader.loadClass(pluginData.entrance) as Class<*>
        if (Fragment::class.java.isAssignableFrom(fragmentCls)) {
            val fragment = fragmentCls.newInstance() as Fragment
            DesolatorInit.activity?.apply {
                supportFragmentManager.beginTransaction().apply {
                    add(android.R.id.content, fragment)
                    commit()
                }
            }
        }
    }
}