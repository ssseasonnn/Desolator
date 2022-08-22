package zlc.season.desolator

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.res.AssetManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import zlc.season.claritypotion.ClarityPotion
import zlc.season.claritypotion.ClarityPotion.application
import zlc.season.desolator.util.logw
import java.lang.reflect.Method
import kotlin.coroutines.suspendCoroutine

@SuppressLint("StaticFieldLeak")
internal object DesolatorHelper {
    internal var isDebug = false

    internal val context = ClarityPotion.context
    internal val classLoader: ClassLoader = ClarityPotion.context.classLoader
    internal val assetManager: AssetManager = ClarityPotion.context.assets

    internal val fragmentActivity: FragmentActivity?
        get() = with(ClarityPotion.activity) {
            if (this != null && this is FragmentActivity) {
                this
            } else {
                null
            }
        }

    internal fun enableReflection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val forName = Class::class.java.getDeclaredMethod("forName", String::class.java)
                val getDeclaredMethod = Class::class.java.getDeclaredMethod(
                    "getDeclaredMethod",
                    String::class.java, arrayOf<Class<*>>()::class.java
                )
                val vmRuntimeClass = forName.invoke(null, "dalvik.system.VMRuntime") as Class<*>
                val getRuntimeMethod =
                    getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null) as Method
                val setHiddenApiMethod = getDeclaredMethod.invoke(
                    vmRuntimeClass, "setHiddenApiExemptions",
                    arrayOf<Class<*>>(Array<String>::class.java)
                ) as Method

                val vmRuntime = getRuntimeMethod.invoke(null)

                setHiddenApiMethod.invoke(vmRuntime, arrayOf("L"))
            } catch (e: Exception) {
                e.logw()
            }
        }
    }

    internal suspend fun awaitActivityCreated() = suspendCoroutine<Activity> {
        application.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                application.unregisterActivityLifecycleCallbacks(this)
                it.resumeWith(Result.success(activity))
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
            }
        })
    }
}