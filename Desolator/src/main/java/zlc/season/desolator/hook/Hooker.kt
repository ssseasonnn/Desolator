package zlc.season.desolator.hook

import android.content.ComponentName
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.*
import android.os.Handler
import zlc.season.desolator.*
import zlc.season.desolator.DesolatorInit.Companion.classLoader
import zlc.season.desolator.DesolatorInit.Companion.context
import java.lang.reflect.Method
import java.lang.reflect.Proxy.newProxyInstance

class Hooker {
    fun init() {
        enableReflection()
        hookAms()
        hookHandler()
        hookPms()
    }

    private fun enableReflection() {
        if (SDK_INT >= P) {
            try {
                val forName = Class::class.java.getDeclaredMethod("forName", String::class.java)
                val getDeclaredMethod = Class::class.java.getDeclaredMethod(
                    "getDeclaredMethod",
                    String::class.java, arrayOf<Class<*>>()::class.java
                )
                val vmRuntimeClass = forName.invoke(null, "dalvik.system.VMRuntime") as Class<*>
                val getRuntimeMethod = getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null) as Method
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


    private fun hookPms() {
        try {
            val fieldPackageManager = Class("android.app.ActivityThread").field("sPackageManager")
            val packageManager = fieldPackageManager.of(null)

            val classIPackageManager = Class("android.content.pm.IPackageManager")

            val proxyPackageManager = newProxyInstance(classLoader, arrayOf(classIPackageManager)) { _, method, args ->
                "PMS hooked! Method -> ${method.name}".logd()
                if (method.name == "getActivityInfo") {
                    val componentName = args[0] as ComponentName
                    if (componentName.className == "zlc.season.pluginb.BPluginActivity") {
                        val newComponentName = ComponentName(
                            componentName.packageName,
                            "zlc.season.desolator.hook.StubActivity"
                        )
                        args[0] = newComponentName
                    }
                }
                method.invoke(packageManager, *args)
            }

            fieldPackageManager.set(null, proxyPackageManager)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hookAms() {
        try {
            val activityManager = when {
                SDK_INT >= Q -> Class("android.app.ActivityTaskManager").field("IActivityTaskManagerSingleton").of(null)
                SDK_INT >= O -> Class("android.app.ActivityManager").field("IActivityManagerSingleton").of(null)
                else -> Class("android.app.ActivityManagerNative").field("gDefault").of(null)
            }

            val fieldSingletonInstance = Class("android.util.Singleton").field("mInstance")
            val methodSingletonGet = Class("android.util.Singleton").method("get")

            val instance = methodSingletonGet.invoke(activityManager)

            val proxyClass = if (SDK_INT >= Q) {
                Class("android.app.IActivityTaskManager")
            } else {
                Class("android.app.IActivityManager")
            }

            val proxyActivityManager = newProxyInstance(classLoader, arrayOf(proxyClass)) { _, method, args ->
                "AMS hooked! Method -> ${method.name}, Args -> $args".logd()
                if (args == null) {
                    return@newProxyInstance method.invoke(instance)
                }

                if (method.name == "startActivity") {
                    var index = 0
                    for (i in args.indices) {
                        if (args[i] is Intent) {
                            index = i
                            break
                        }
                    }
                    val proxyIntent = Intent()
                    proxyIntent.setClassName(context.packageName, "zlc.season.desolator.hook.StubActivity")
                    proxyIntent.putExtra("oldIntent", args[index] as Intent)
                    args[index] = proxyIntent
                }
                method.invoke(instance, *args)
            }
            fieldSingletonInstance.set(activityManager, proxyActivityManager)
        } catch (e: Exception) {
            e.logw()
        }
    }

    private fun hookHandler() {
        try {
            val activityThread = Class("android.app.ActivityThread").field("sCurrentActivityThread").of(null)
            val h = Class("android.app.ActivityThread").field("mH").of(activityThread)

            val fieldHandlerCallback = Class("android.os.Handler").field("mCallback")

            fieldHandlerCallback[h] = Handler.Callback { msg ->
                try {
                    "ActivityThread handler hooked! Msg -> ${msg.what}".logd()
                    when (msg.what) {
                        100 -> {
                            val proxyIntent = msg.obj.javaClass.field("intent").of(msg.obj) as Intent
                            val intent = proxyIntent.getParcelableExtra<Intent>("oldIntent")
                            intent?.let {
                                proxyIntent.component = it.component
                            }
                        }
                        159 -> {
                            val mActivityCallbacks = msg.obj.javaClass.field("mActivityCallbacks").of(msg.obj) as List<*>
                            mActivityCallbacks.forEach {
                                val itemClass = it?.javaClass
                                if (itemClass?.name == "android.app.servertransaction.LaunchActivityItem") {
                                    val proxyIntent = itemClass.field("mIntent").of(it) as Intent
                                    val intent = proxyIntent.getParcelableExtra<Intent>("oldIntent")
                                    intent?.let {
                                        proxyIntent.component = it.component
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.logw()
                }
                // Must return false
                false
            }
        } catch (e: Exception) {
            e.logw()
        }
    }
}