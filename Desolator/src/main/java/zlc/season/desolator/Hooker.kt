package zlc.season.desolator

//import android.os.Build.VERSION_CODES.O
//import android.os.Build.VERSION_CODES.Q
import android.content.ComponentName
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Handler
import android.util.Log
import java.lang.Thread.currentThread
import java.lang.reflect.Method
import java.lang.reflect.Proxy.newProxyInstance

object Hooker {
    fun enableSuperReflection() {
        if (SDK_INT >= 28) {
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
            }
        }
    }


    fun hookPms() {
        try {
            val activityThreadClass = Class("android.app.ActivityThread")
            val packageManagerField = activityThreadClass.field("sPackageManager")
            val packageManager = activityThreadClass.field("sPackageManager").of(null)

            val IPackageManagerClass = Class("android.content.pm.IPackageManager")
            val proxyPms = newProxyInstance(
                currentThread().contextClassLoader,
                arrayOf(IPackageManagerClass)
            ) { proxy, method, args ->
                println("hook pms: ${method.name}")
                if (method.name == "getActivityInfo") {
                    val componentName = args[0] as ComponentName
                    if (componentName.className == "zlc.season.pluginb.BPluginActivity") {
                        val newComponentName = ComponentName(
                            componentName.packageName,
                            "zlc.season.desolator.StubActivity"
                        )
                        args[0] = newComponentName
                    }
                    println("args: ${args[0].toString()}")
                }
                method.invoke(packageManager, *args)
            }

            packageManagerField.set(null, proxyPms)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hookAms() {
        try {
            val singleTon = when {
                SDK_INT >= 29 -> Class("android.app.ActivityTaskManager").field("IActivityTaskManagerSingleton")
                    .of(null)
                SDK_INT >= 26 -> Class("android.app.ActivityManager").field("IActivityManagerSingleton")
                    .of(null)
                else -> Class("android.app.ActivityManagerNative").field("gDefault").of(null)
            }
            val mInstanceField = Class("android.util.Singleton").field("mInstance")
            val method = Class("android.util.Singleton").method("get")
            val mInstanceObj = method.invoke(singleTon)
//
            val proxyClass =
                if (SDK_INT >= 29) Class("android.app.IActivityTaskManager") else Class("android.app.IActivityManager")
            val newInstance = newProxyInstance(
                currentThread().contextClassLoader,
                arrayOf(proxyClass)
            ) { proxy, method, args ->
                if (method.name == "startActivity") {
                    var index = 0
                    for (i in args.indices) {
                        if (args[i] is Intent) {
                            index = i
                            break
                        }
                    }
                    val proxyIntent = Intent()
                    proxyIntent.setClassName(
                        HookerInit.context.packageName,
                        "zlc.season.desolator.StubActivity"
                    )
                    proxyIntent.putExtra("oldIntent", args[index] as Intent)
                    args[index] = proxyIntent
                }
                method.invoke(mInstanceObj, *args)
            }
            mInstanceField.set(singleTon, newInstance)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hookHandler() {
        try {
            // 获取ActivityThread实例
            val activityThreadClass = Class("android.app.ActivityThread")
            val activityThread = activityThreadClass.field("sCurrentActivityThread").of(null)

            // 获取Handler实例
            val mH = activityThreadClass.field("mH").of(activityThread)

            val mCallbackField = Class("android.os.Handler").field("mCallback")

            mCallbackField[mH] = Handler.Callback { msg ->
                Log.e("kangf", "handling code = " + msg.what)
                when (msg.what) {
                    100 -> try {
                        // 获取ActivityClientRecord中的intent对象
                        val proxyIntent = msg.obj.javaClass.field("intent").of(msg.obj) as Intent
                        // 拿到插件的Intent
                        val intent = proxyIntent.getParcelableExtra<Intent>("oldIntent")
                        // 替换回来
                        proxyIntent.component = intent!!.component
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    159 -> try {
                        val mActivityCallbacks =
                            msg.obj.javaClass.field("mActivityCallbacks").of(msg.obj) as List<*>
                        mActivityCallbacks.forEach {
                            val itemClass = it?.javaClass
                            if (itemClass?.name == "android.app.servertransaction.LaunchActivityItem") {
                                val proxyIntent = itemClass.field("mIntent").of(it) as Intent
                                val intent = proxyIntent.getParcelableExtra<Intent>("oldIntent")
                                if (intent != null) {
                                    proxyIntent.component = intent.component
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Must return false
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}