package info.macias.codebuilder.builder

import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.apache.maven.cli.MavenCli
import org.slf4j.LoggerFactory
import java.io.File
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintStream
import java.util.*

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
class BuilderVerticle(val address : String, val m2Home:String) : AbstractVerticle() {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun start() {
        vertx.eventBus().consumer<String>(address, { msgHandler ->
            val projectRoot = msgHandler.body()
            logger.info("Compiling project at folder: $projectRoot")

            System.setProperty("maven.multiModuleProjectDirectory",m2Home)

            val output = PipedOutputStream()
            val input = PipedInputStream(output)
            val print = PrintStream(output,true)

            val cli = MavenCli()
            var result : Int? = null
            Thread(Runnable {
                result = cli.doMain(arrayOf("clean","package","-e","-B"),
                        projectRoot, print, print)
                print.close()
            }).start();

            var sb = StringBuilder()
            var data = input.read()
            while(data >= 0) {
                sb.append(data.toChar())
                data = input.read()
            }

            sb.append('\n').append("Maven process returned $result")
            if(result == 0) {
                val targetDir = File("$projectRoot/target")
                val files = JsonArray()
                targetDir.list { file, name -> name.endsWith(".war") || name.endsWith(".jar") || name.endsWith(".ear") }
                    .forEach { files.add("$projectRoot/target/$it"); }
                msgHandler.reply(JsonObject().put("files", files).put("out", sb.toString()))
            } else {
                msgHandler.fail(result!!,sb.toString())
            }
        })
    }
}