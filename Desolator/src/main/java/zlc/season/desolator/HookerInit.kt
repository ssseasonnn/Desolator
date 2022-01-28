package zlc.season.desolator

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri

class HookerInit : ContentProvider() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun attachInfo(context: Context, info: ProviderInfo?) {
        super.attachInfo(context, info)
        HookerInit.context = context

//        Hooker.hookInstrumentation((context as Application).baseContext)
    }

    override fun onCreate(): Boolean {
        Hooker.enableSuperReflection()
        Hooker.hookAms()
        Hooker.hookHandler()

        context?.let {
            LoadUtils.init(it)
            LoadUtils.load(it)
            LoadUtils.loadAsset(it)
        }

        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
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