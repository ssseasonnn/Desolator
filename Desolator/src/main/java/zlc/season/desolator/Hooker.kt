package zlc.season.desolator

import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.Build.VERSION_CODES.Q
import android.os.Handler
import android.util.Log
import java.lang.Thread.currentThread
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.lang.reflect.Proxy.newProxyInstance

object Hooker {
    fun enableSuperReflection() {
        if (SDK_INT >= Build.VERSION_CODES.P) {
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
            }
        }
    }


    fun hookInstrumentation(context: Context) {
        val mainThreadObj = Class("android.app.ContextImpl").field("mMainThread").of(context)

        val instrumentationField = Class("android.app.ActivityThread").field("mInstrumentation")
        instrumentationField.proxy(mainThreadObj) {
            InstrumentationProxy(context, it as Instrumentation, context.packageManager)
        }
    }

    fun hookAms() {
        try {
            val singleTon = when {
                SDK_INT >= Q -> Class("android.app.ActivityTaskManager").field("IActivityTaskManagerSingleton").of(null)
                SDK_INT >= O -> Class("android.app.ActivityManager").field("IActivityManagerSingleton").of(null)
                else -> Class("android.app.ActivityManagerNative").field("gDefault").of(null)
            }
            val mInstanceField = Class("android.util.Singleton").field("mInstance")
            val mInstanceObj = mInstanceField.of(singleTon)

            val proxyClass =
                if (SDK_INT >= Q) Class("android.app.IActivityTaskManager") else Class("android.app.IActivityManager")
            val newInstance = newProxyInstance(currentThread().contextClassLoader, arrayOf(proxyClass)) { proxy, method, args ->
                if (method.name == "startActivity") {
                    var index = 0
                    for (i in args.indices) {
                        if (args[i] is Intent) {
                            index = i
                            break
                        }
                    }
                    val proxyIntent = Intent()
                    proxyIntent.setClassName("com.kangf.dynamic", "com.kangf.dynamic.ProxyActivity")
                    proxyIntent.putExtra("oldIntent", args[index] as Intent)
                    args[index] = proxyIntent
                }
                method.invoke(mInstanceObj, *args)
            }
            mInstanceField.set(mInstanceObj, newInstance)

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
                        val mActivityCallbacks = msg.obj.javaClass.field("mActivityCallbacks").of(msg.obj) as List<*>
                        mActivityCallbacks.forEach {
                            val itemClass = it?.javaClass
                            if (itemClass?.name == "android.app.servertransaction.LaunchActivityItem") {
                                val proxyIntent = itemClass.field("mIntent").of(it) as Intent
                                val intent = proxyIntent.getParcelableExtra<Intent>("oldIntent")
                                proxyIntent.component = intent!!.component
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