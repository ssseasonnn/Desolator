package zlc.season.desolator.util

import android.util.Log.*


private var DEBUG = true
private var LOG_TAG = "Desolator"

fun setDesolatorLogTag(tag: String) {
    LOG_TAG = tag
}

fun setDesolatorDebug(flag: Boolean) {
    DEBUG = flag
}

internal fun <T> T.logd(tag: String = ""): T {
    realLog(::d, ::d, tag)
    return this
}

internal fun <T> T.logi(tag: String = ""): T {
    realLog(::i, ::i, tag)
    return this
}

internal fun <T> T.logw(tag: String = ""): T {
    realLog(::w, ::w, tag)
    return this
}

internal fun <T> T.loge(tag: String = ""): T {
    realLog(::e, ::e, tag)
    return this
}

internal fun <T> T.logv(tag: String = ""): T {
    realLog(::v, ::v, tag)
    return this
}

private fun <T> T.realLog(
    function1: (String, String, Throwable) -> Int,
    function2: (String, String) -> Int,
    tag: String
) {
    if (DEBUG) {
        val realTag = tag.ifEmpty { LOG_TAG }
        if (this is Throwable) {
            function1(realTag, this.message ?: "", this)
        } else {
            function2(realTag, this.toString())
        }
    }
}