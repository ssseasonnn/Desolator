package zlc.season.desolator.util

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

internal fun InputStream.copy(dest: File) {
    dest.parentFile?.checkDir()
    this.use {
        val outputStream = FileOutputStream(dest)
        outputStream.use {
            val buffer = ByteArray(1024)
            var count = read(buffer)
            while (count > 0) {
                it.write(buffer, 0, count)
                count = read(buffer)
            }
            it.flush()
        }
    }
}

internal fun File.checkDir() {
    if (!exists()) {
        mkdirs()
    } else if (!isDirectory) {
        delete()
        mkdirs()
    }
}