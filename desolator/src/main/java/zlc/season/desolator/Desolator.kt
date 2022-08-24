package zlc.season.desolator

import android.app.Activity
import android.os.Bundle
import kotlinx.coroutines.*
import zlc.season.desolator.DesolatorHelper.awaitActivityCreated
import zlc.season.desolator.DesolatorHelper.fragmentActivity
import zlc.season.desolator.util.*
import zlc.season.downloadx.Progress
import zlc.season.downloadx.State
import zlc.season.downloadx.utils.LOG_ENABLE

object Desolator {
    private val pluginManager by lazy { PluginManager() }
    private val pluginLoader by lazy { PluginLoader() }

    private val coroutineScope by lazy { MainScope() }
    private val pluginDownloader by lazy { DefaultPluginDownloader(coroutineScope) }

    private var initJob: Job? = null

    suspend fun awaitInit() {
        initJob?.join()
    }

    fun init(isDebug: Boolean = false): Job {
        DesolatorHelper.isDebug = isDebug
        LOG_ENABLE = isDebug

        return coroutineScope.launch(Dispatchers.Main.immediate) {
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

    fun downloadPlugin(
        pluginData: PluginData,
        onProgress: (Progress) -> Unit = {},
        onFailed: () -> Unit = {},
        onSuccess: () -> Unit = {}
    ): Job {
        return coroutineScope.launch {
            if (pluginData.isPluginExists()) {
                onSuccess()
                return@launch
            }
            realDownloadPlugin(pluginData, onProgress, onFailed, onSuccess)
        }
    }

    private suspend fun realDownloadPlugin(
        pluginData: PluginData,
        onProgress: (Progress) -> Unit = {},
        onFailed: () -> Unit = {},
        onSuccess: suspend () -> Unit = {}
    ) {
        val flow = pluginDownloader.startDownload(pluginData)
        flow.collect {
            when (it) {
                is State.Downloading -> onProgress(it.progress)
                is State.Failed, is State.Stopped -> onFailed()
                is State.Succeed -> onSuccess()
                else -> {}
            }
        }
    }

    fun deletePlugin(pluginData: PluginData, deleteAll: Boolean = false) {
        if (deleteAll) {
            val dir = pluginData.pluginBaseDir()
            dir.listFiles()?.forEach {
                it.deleteRecursively()
            }
        } else {
            pluginData.pluginFile().delete()
        }
    }

    fun installPlugin(
        pluginData: PluginData,
        onProgress: (Progress) -> Unit = {},
        onFailed: () -> Unit = {},
        onSuccess: () -> Unit = {}
    ): Job {
        val alreadyDownloaded = pluginData.isPluginExists()
        val job = coroutineScope.launch {
            if (alreadyDownloaded) {
                realInstallPlugin(pluginData)
                onSuccess()
            } else {
                realDownloadPlugin(pluginData, onProgress, onFailed) {
                    realInstallPlugin(pluginData)
                    onSuccess()
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
        if (isPluginLoaded(pluginData)) {
            "$pluginData already installed!".logw()
            return
        }
        withContext(Dispatchers.IO) {
            val pluginInfo = pluginManager.createPlugin(pluginData)
            pluginInfo?.let {
                pluginLoader.loadPlugin(it)
            }
        }
    }

    private fun isPluginLoaded(pluginData: PluginData): Boolean {
        return pluginLoader.isPluginLoaded(pluginData.id)
    }

    fun startPlugin(
        pluginData: PluginData,
        params: Bundle? = null,
        isReplace: Boolean = true,
        addToBackStack: Boolean = true
    ) {
        if (!isPluginLoaded(pluginData)) {
            "$pluginData not install!".logw()
            return
        }

        if (pluginData.entrance.isEmpty()) {
            "$pluginData hasn't an entrance!".logw()
            return
        }

        val fragmentActivity = fragmentActivity
        if (fragmentActivity == null) {
            "No FragmentActivity found, so plugin can't start.".logw()
            return
        }

        val fragment = pluginData.createEntranceFragment(fragmentActivity, params)
        val fm = fragmentActivity.supportFragmentManager
        if (fm.isDestroyed) return

        fm.beginTransaction().apply {
            if (addToBackStack) {
                addToBackStack(null)
            }
            if (isReplace) {
                replace(android.R.id.content, fragment)
            } else {
                add(android.R.id.content, fragment)
            }
            if (fm.isStateSaved) {
                commitAllowingStateLoss()
            } else {
                commit()
            }
        }
    }
}