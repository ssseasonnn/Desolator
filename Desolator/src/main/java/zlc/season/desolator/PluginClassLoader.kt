package zlc.season.desolator

import dalvik.system.DexClassLoader

class PluginClassLoader(
    dexPath: String?,
    optimizedDirectory: String?,
    librarySearchPath: String?,
    parent: ClassLoader?
) : DexClassLoader(dexPath, optimizedDirectory, librarySearchPath, parent) {

    override fun loadClass(name: String?, resolve: Boolean): Class<*> {
        return super.loadClass(name, resolve)
    }
}