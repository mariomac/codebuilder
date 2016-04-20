package info.macias.codebuilder.file

import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject
import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
class FileServerVerticle(val address: String) : AbstractVerticle() {
    // key : file Id, value : file path
    private val fileMap = HashMap<String,String>()
    override fun start() {
        vertx.eventBus().consumer<JsonObject>(address, { message ->
            val body = message.body()
            val cmd = body.getString("cmd")
            if(cmd == "put") {
                fileMap.put(body.getString("id"), body.getString("path"))
            } else if(cmd == "get") {
                val file = File(fileMap.get(body.getString("id")))
                val fis = FileInputStream(file)
                val bytes = ByteArray(file.length().toInt())
                var read = fis.read(bytes)
                message.reply(bytes)
            } else if(cmd == "name") {
                val path = fileMap.get(body.getString("id"))
                message.reply(path?.substring(path.lastIndexOf(File.pathSeparatorChar)+1))
            }
        })
    }

}