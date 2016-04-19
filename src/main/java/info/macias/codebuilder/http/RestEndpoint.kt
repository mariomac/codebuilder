package info.macias.codebuilder.http

import info.macias.codebuilder.AppController
import info.macias.codebuilder.Cfg
import info.macias.codebuilder.auth.DbAuthProviderClient
import info.macias.codebuilder.auth.msg.CredentialsCall
import info.macias.codebuilder.console.ConsoleManager
import info.macias.codebuilder.zip.ZipChunksCodec
import info.macias.kutils.intToByteArray
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.FileUpload
import io.vertx.ext.web.Router
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Created by mmacias on 30/3/16.
 */
internal class RestEndpoint {
    val consoleManager = ConsoleManager()

    val logger = LoggerFactory.getLogger(RestEndpoint::class.java)
    fun register(router: Router, vertx: Vertx) {

        // rest api key authentication
        router.route("/rest/*").handler { ctx ->
            var apiKey:String? = ctx.request().getHeader("X-ApiKey")
            if(apiKey == null) {
                apiKey = ctx.request().getParam("apikey")
            }
            DbAuthProviderClient(vertx.eventBus())
                    .authenticate(CredentialsCall.toJsonObject(CredentialsCall(apikey = apiKey))) {
                        login ->
                        if(login.failed()) {
                            ctx.fail(401);
                        } else {
                            ctx.setUser(login.result())
                            ctx.next()
                        }
                    }
        }

        router.get("/rest/console").handler { ctx ->
            consoleManager.listen(ctx.user().principal().getString("name"),ctx.request())
        }

        router.get("/rest/hello").handler { ctx ->
            consoleManager.writeln(ctx.user().principal().getString("name"), ctx.user().principal().getString("name") + " said hello")
            ctx.response().end()
        }

        router.post("/rest/dropfile").handler({ ctx ->
            var userName = ctx.user().principal().getString("name")
            var file : FileUpload? = null
            for (f in ctx.fileUploads()) {
                file = f
                consoleManager.writeln(userName, "Filename: ${f.fileName()}\n");
                var size = f.size().toDouble()
                val (sizeStr,unit) = if(size / 1000000000 > 0.1) {
                    Pair(String.format("%.2f",size/1000000000), "GB")
                } else if(size / 1000000 > 0.1) {
                    Pair(String.format("%.2f",size/1000000), "MB")
                } else if(size / 1000 > 0.1) {
                    Pair(String.format("%.2f",size/1000), "KB")
                } else{
                    Pair(size.toString(),"B")
                }
                consoleManager.writeln(userName, "Size: ${sizeStr} $unit\n");
                break; // we consider only one file
            }
            try {
                if(file == null) throw NullPointerException("There is no file");

                consoleManager.writeln(userName, "Unzipping file... This task can take long depending on the size of the file")
                vertx.eventBus().send<JsonArray>(
                        AppController.Verticles.ZIP.address,
                        file.uploadedFileName(),
                        {
                            if(it.succeeded()) {
                                consoleManager.writeln(userName, "Sending file to Zip worker: Succeeded ")
                                val files = it.result().body()
                                consoleManager.writeln(userName, "${files.size()} entries have been extracted to:\n\t${files.getString(0)}")
                                consoleManager.writeln(userName, "Building...")
                                vertx.eventBus().send<String>(AppController.Verticles.BUILDER.address,
                                        files.getString(0),
                                        { replyHandler->
                                            consoleManager.writeln(userName,replyHandler.result().body())
                                            if(replyHandler.failed()) {
                                                consoleManager.writeln(userName,"ERROR: The build process failed")
                                            }
                                        })
                            }
                            if(it.failed()) {
                                consoleManager.writeln(userName, "Sending file to Zip worker: Failed")
                                consoleManager.writeln(userName, "Sending file to Zip worker: cause:  " + it.cause())
                            }

                        });

            } catch(t: Throwable) {
                logger.debug(t.message,t)
                ctx.response().statusCode = 500;
                consoleManager.writeln(userName, "ERROR: ${t.message}");
            }
            // use the same redirection technique as gui to allow download file
            // o mirar metodo "sendfile"
            ctx.response().end();
        });
    }

}
