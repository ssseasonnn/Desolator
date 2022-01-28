package zlc.season.desolatordemo

import java.io.File
import java.util.zip.ZipFile


fun File.unzip(unzipPath: String): Boolean {
    val unzipFolder = File(unzipPath)
    if (!unzipFolder.exists()) {
        unzipFolder.mkdirs()
    }

    try {
        ZipFile(this).use { zip ->
            zip.entries().asSequence()
                .map {
                    val outputFile = File(unzipFolder.absolutePath + File.separator + it.name)
                    Pair(it, outputFile)
                }
                .map {
                    it.apply {
                        second.parentFile?.run {
                            if (!exists()) mkdirs()
                        }
                    }
                }
                .filter { !it.first.isDirectory }
                .forEach { (entry, output) ->
                    zip.getInputStream(entry).use { input ->
                        output.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
        }
    } catch (e: Exception) {
        unzipFolder.delete()
        return false
    }
    return true
}