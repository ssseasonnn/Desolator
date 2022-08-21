package zlc.season.desolator.hook

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.*
import android.os.Handler
import zlc.season.desolator.*
import zlc.season.desolator.util.*
import java.lang.reflect.Method
import java.lang.reflect.Proxy.newProxyInstance

class Hooker {
    companion object {
        const val KEY_REAL_INTENT = "key_desolator_real_intent"
    }

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


    private fun hookPms() {
        try {
            val fieldPackageManager = Class("android.app.ActivityThread").field("sPackageManager")
            val packageManager = fieldPackageManager.of(null)

            val classIPackageManager = Class("android.content.pm.IPackageManager")

            val proxyPackageManager = newProxyInstance(
                DesolatorInit.classLoader,
                arrayOf(classIPackageManager)
            ) { _, method, args ->
//                "PMS hooked! Method -> ${method.name}, Args -> ${args?.toList()}".logd()
                if (args == null) {
                    return@newProxyInstance method.invoke(packageManager)
                }
                if (method.name != "getActivityInfo") {
                    return@newProxyInstance method.invoke(packageManager, *args)
                }

                val result = try {
                    method.invoke(packageManager, *args)
                } catch (e: Exception) {
                    null
                }
                if (result == null) {
                    val oldComponentName = args[0] as ComponentName
                    val newComponentName = ComponentName(
                        oldComponentName.packageName,
                        DesolatorActivity::class.java.name
                    )
                    args[0] = newComponentName
                    "PMS getActivityInfo hooked! Old -> $oldComponentName, New -> $newComponentName".logd()
                    return@newProxyInstance method.invoke(packageManager, *args)
                } else {
                    return@newProxyInstance result
                }
            }

            //replace
            fieldPackageManager.set(null, proxyPackageManager)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hookAms() {
        try {
            val activityManager = when {
                SDK_INT >= Q -> Class("android.app.ActivityTaskManager").field("IActivityTaskManagerSingleton")
                    .of(null)
                SDK_INT >= O -> Class("android.app.ActivityManager").field("IActivityManagerSingleton")
                    .of(null)
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

            val proxyActivityManager = newProxyInstance(
                DesolatorInit.classLoader,
                arrayOf(proxyClass)
            ) { _, method, args ->
//                "AMS hooked! Method -> ${method.name}, Args -> ${args?.toList()}".logd()
                if (args == null) {
                    return@newProxyInstance method.invoke(instance)
                }

                if (method.name == "startActivity") {
                    var index = -1
                    for (i in args.indices) {
                        if (args[i] is Intent) {
                            index = i
                            break
                        }
                    }
                    if (index >= 0) {
                        val realIntent = args[index] as Intent
                        val realComponentName = realIntent.component
                        if (realComponentName != null) {
                            val activityInfo = try {
                                DesolatorInit.context.packageManager.getActivityInfo(
                                    realComponentName,
                                    PackageManager.GET_META_DATA
                                )
                            } catch (e: Exception) {
                                null
                            }
                            if (activityInfo == null) {
                                val proxyIntent = Intent()
                                proxyIntent.setClass(
                                    DesolatorInit.context,
                                    DesolatorActivity::class.java
                                )
                                proxyIntent.putExtra(KEY_REAL_INTENT, realIntent)
                                args[index] = proxyIntent

                                "AMS startActivity hooked! Old -> $realComponentName, New -> ${proxyIntent.component}".logd()
                            }
                        }
                    }
                }

                method.invoke(instance, *args)
            }

            //replace activity manager
            fieldSingletonInstance.set(activityManager, proxyActivityManager)
        } catch (e: Exception) {
            e.logw()
        }
    }

    private fun hookHandler() {
        try {
            val activityThread =
                Class("android.app.ActivityThread").field("sCurrentActivityThread").of(null)
            val h = Class("android.app.ActivityThread").field("mH").of(activityThread)

            val fieldHandlerCallback = Class("android.os.Handler").field("mCallback")
            fieldHandlerCallback[h] = Handler.Callback { msg ->
                try {
//                    "ActivityThread handler hooked! Msg -> $msg".logd()
                    when (msg.what) {
                        100 -> {
                            val proxyIntent =
                                msg.obj.javaClass.field("intent").of(msg.obj) as Intent
                            val intent = proxyIntent.getParcelableExtra<Intent>(KEY_REAL_INTENT)
                            intent?.let {
                                "ActivityThread hooked! Old -> ${proxyIntent.component}, New -> ${it.component}".logd()
                                proxyIntent.component = it.component
                            }
                        }
                        159 -> {
                            val activityCallbacks =
                                msg.obj.javaClass.field("mActivityCallbacks").of(msg.obj) as List<*>
                            activityCallbacks.filterNotNull().forEach { each ->
                                if (each.javaClass.name == "android.app.servertransaction.LaunchActivityItem") {
                                    val proxyIntent =
                                        each.javaClass.field("mIntent").of(each) as Intent
                                    val intent =
                                        proxyIntent.getParcelableExtra<Intent>(KEY_REAL_INTENT)
                                    intent?.let {
                                        "ActivityThread hooked! Old -> ${proxyIntent.component}, New -> ${it.component}".logd()
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