package zlc.season.desolator

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.content.res.AssetManager
import android.database.Cursor
import android.net.Uri
import zlc.season.desolator.hook.Hooker

class DesolatorInit : ContentProvider() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        @SuppressLint("StaticFieldLeak")
        lateinit var contextImpl: Context

        lateinit var classLoader: ClassLoader

        lateinit var assetManager: AssetManager
    }

    override fun attachInfo(context: Context, info: ProviderInfo?) {
        super.attachInfo(context, info)
        DesolatorInit.context = context
        contextImpl = (context as Application).baseContext
        classLoader = context.classLoader
        assetManager = context.assets

        Hooker().init()
        Desolator.installInternalPlugin()
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }
}