package zlc.season.desolator

import dalvik.system.PathClassLoader

class SuperClassLoader(dexPath: String?, parent: ClassLoader?) : PathClassLoader(dexPath, parent) {
    val pluginClassLoaderList = mutableListOf<ClassLoader>()

    fun addPluginClassLoader(classLoader: ClassLoader) {
        pluginClassLoaderList.add(classLoader)
    }

    override fun loadClass(name: String?, resolve: Boolean): Class<*> {
        var result: Class<*>? = null
        try {
            result = super.loadClass(name, resolve)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (result != null) return result

        try {
            pluginClassLoaderList.forEach {
                result = it.loadClass(name)
                if (result != null) return result as Class<*>
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        throw  ClassNotFoundException("$name in loader $this");
    }
}