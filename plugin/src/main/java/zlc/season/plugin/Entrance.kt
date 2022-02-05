package zlc.season.plugin

class Bootstrap {
    var apkPath: String = ""
    var classLoader: ClassLoader? = null

    fun init(apkPath: String, classLoader: ClassLoader) {
        this.apkPath = apkPath
        this.classLoader = classLoader
        if (this.apkPath.isEmpty() || this.classLoader == null) {
            println("invalid plugin")
            return
        }
    }


}

class Entrance {

    var apkPath: String = ""
    var classLoader: ClassLoader? = null

    fun start() {

    }
}