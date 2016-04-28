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
                val id = UUID.randomUUID().toString()
                fileMap.put(id, body.getString("path"))
                message.reply(id)
            } else if(cmd == "get") {
                val file = File(fileMap.get(body.getString("id")))
                val fis = FileInputStream(file)
                val bytes = ByteArray(file.length().toInt())
                fis.read(bytes)
                message.reply(bytes)
            } else if(cmd == "name") {
                val path = fileMap.get(body.getString("id"))
                val name = path?.substring(path.lastIndexOf(File.separatorChar)+1)
                println("Returning name : $name")
                message.reply(name)
            }
        })
    }

}