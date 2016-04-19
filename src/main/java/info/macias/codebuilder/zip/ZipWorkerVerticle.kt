package info.macias.codebuilder.zip

import info.macias.kutils.copy
import info.macias.kutils.mkdir
import io.vertx.core.AbstractVerticle
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.FileUpload
import java.io.ByteArrayInputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
class ZipWorkerVerticle(val address : String, val dstFolder:String) : AbstractVerticle() {

    private val rnd = Random(System.currentTimeMillis())

    override fun start() {
        /*
         * Returns the list of files and directories that have been uncompressed
         * The first entry is the directory where all the files have been uncompressed
         */
        vertx.eventBus().consumer<String>(address, { uploadedFileName ->
            try {
                val dstFolder = "${this.dstFolder}${if(this.dstFolder.endsWith("/")) "job" else "/job"}${rnd.nextLong()}"
                mkdir(dstFolder)
                val files = JsonArray()
                files.add(dstFolder)
                val file = ZipFile(uploadedFileName.body())
                for(entry:ZipEntry in file.entries()) {
                    val dstFile = "$dstFolder/${entry.name}"
                    files.add(dstFile)
                    if(entry.isDirectory) {
                        mkdir(dstFile);
                    } else {
                        copy(file.getInputStream(entry), dstFile)
                    }
                }
                file.close()
                uploadedFileName.reply(files)
            } catch(e:Exception) {
                uploadedFileName.fail(-1,e.message)
            }
        });
    }
}