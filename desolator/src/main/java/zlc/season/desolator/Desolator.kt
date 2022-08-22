package zlc.season.desolator

import android.app.Activity
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import zlc.season.desolator.DesolatorHelper.awaitActivityCreated
import zlc.season.desolator.DesolatorHelper.classLoader
import zlc.season.desolator.util.logw
import zlc.season.desolator.util.pluginFile
import zlc.season.downloadx.Progress
import zlc.season.downloadx.State

object Desolator {
    private val pluginManager by lazy { PluginManager() }
    private val pluginLoader by lazy { PluginLoader() }

    private val pluginDownloader by lazy { DefaultPluginDownloader() }
    private val coroutineScope by lazy { GlobalScope }

    private var initJob: Job? = null

    suspend fun awaitInit() {
        initJob?.join()
    }

    fun init(isDebug: Boolean = false): Job {
        DesolatorHelper.isDebug = isDebug

        return coroutineScope.launch {
            val firstActivity = awaitActivityCreated()
            installInternalPlugin(firstActivity)
        }.also { initJob = it }
    }

    private suspend fun installInternalPlugin(activity: Activity) {
        withContext(Dispatchers.IO) {
            val pluginList = pluginManager.initInternalPlugin()
            pluginList.forEach {
                pluginLoader.loadPluginInternal(it, activity)
            }
        }
    }

    fun installPlugin(
        pluginData: PluginData,
        onFailed: () -> Unit = {},
        onSuccess: () -> Unit = {},
        onProgress: (Progress) -> Unit = {}
    ): Job {
        val alreadyDownloaded = pluginData.pluginFile().exists()
        val job = coroutineScope.launch {
            if (alreadyDownloaded) {
                realInstallPlugin(pluginData)
                onSuccess()
            } else {
                val flow = pluginDownloader.startDownload(pluginData)
                flow.collect {
                    when (it) {
                        is State.Downloading -> onProgress(it.progress)
                        is State.Failed, is State.Stopped -> onFailed()
                        is State.Succeed -> {
                            realInstallPlugin(pluginData)
                            onSuccess()
                        }
                        else -> {}
                    }
                }
            }
        }
        job.invokeOnCompletion {
            if (it != null && !alreadyDownloaded) {
                pluginDownloader.cancelDownload(pluginData)
            }
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
            DesolatorHelper.fragmentActivity?.apply {
                supportFragmentManager.beginTransaction().apply {
                    add(android.R.id.content, fragment)
                    commit()
                }
            }
        }
    }
}