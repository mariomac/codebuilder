package info.macias.kutils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Created by mmacias on 15/4/16.
 */
fun copy(srcPath:String,dstPath:String) = FileInputStream(srcPath).use { copy(it, dstPath) }

fun copy(src: InputStream, dstPath: String) =
    FileOutputStream(dstPath, false).use { dst ->
        val bytes = ByteArray(1024)
        var read = src.read(bytes)
        while(read != -1) {
            dst.write(bytes,0,read)
            read = src.read(bytes)
        }
    }

fun copy(src: FileInputStream, dstPath:String) =
    FileOutputStream(dstPath, false).use {
        src.channel.transferTo(0,Long.MAX_VALUE,it.channel)
    }


fun mkdir(path:String) {
    File(path).mkdirs()
}