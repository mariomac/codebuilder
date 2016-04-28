package info.macias.codebuilder.http

import info.macias.codebuilder.AppController
import info.macias.codebuilder.Cfg
import info.macias.codebuilder.auth.DbAuthProviderClient
import info.macias.codebuilder.auth.msg.CredentialsCall
import info.macias.codebuilder.console.EventsManager
import info.macias.codebuilder.zip.ZipChunksCodec
import info.macias.kutils.intToByteArray
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.FileUpload
import io.vertx.ext.web.Router
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Created by mmacias on 30/3/16.
 */
internal class RestEndpoint {
    val clientEventsManager = EventsManager()

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

        router.get("/rest/events").handler { ctx ->
            clientEventsManager.listen(ctx.request().getParam("sessionId"),ctx.request())
        }

        router.post("/rest/dropfile").handler({ ctx ->
            val sessionId = ctx.request().getParam("sessionId")
            var file : FileUpload? = null
            for (f in ctx.fileUploads()) {
                file = f
                clientEventsManager.writeln(sessionId, "Filename: ${f.fileName()}\n");
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
                clientEventsManager.writeln(sessionId, "Size: ${sizeStr} $unit\n");
                break; // we consider only one file
            }
            try {
                if(file == null) throw NullPointerException("There is no file");

                clientEventsManager.writeln(sessionId, "Unzipping file... This task can take long depending on the size of the file")
                vertx.eventBus().send<JsonArray>(
                        AppController.Verticles.ZIP.address,
                        file.uploadedFileName(),
                        {
                            if(it.succeeded()) {
                                val files = it.result().body()
                                clientEventsManager.writeln(sessionId, " ${files.size()} entries")
                                logger.debug("${files.size()} entries have been extracted to:\n\t${files.getString(0)}")
                                clientEventsManager.writeln(sessionId, "Building...")
                                vertx.eventBus().send<JsonObject>(AppController.Verticles.BUILDER.address,
                                        files.getString(0),
                                        DeliveryOptions().setSendTimeout(Cfg.buildTimeout),
                                        { replyHandler->
                                            if(replyHandler.failed()) {
                                                clientEventsManager.writeln(sessionId,"Build failed: ${replyHandler.cause()}")
                                            } else {
                                                val result = replyHandler.result().body()
                                                clientEventsManager.writeln(sessionId,result.getString("out"))
                                                if(replyHandler.failed()) {
                                                    clientEventsManager.writeln(sessionId,"ERROR: The build process failed")
                                                } else {
                                                    // notify file server and then return back file IDs to the client
                                                    result.getJsonArray("files").forEach { filePath ->
                                                        vertx.eventBus().send<String>(AppController.Verticles.FILESERVER.address,
                                                                JsonObject().put("cmd","put").put("path",filePath.toString()),
                                                                { fileId ->
                                                                    clientEventsManager.onFileReady(sessionId,fileId.result().body())
                                                                }
                                                        )
                                                    }
                                                }
                                            }
                                        })
                            }
                            if(it.failed()) {
                                clientEventsManager.writeln(sessionId, "Sending file to Zip worker: Failed")
                                clientEventsManager.writeln(sessionId, "Sending file to Zip worker: cause:  " + it.cause())
                            }

                        });

            } catch(t: Throwable) {
                logger.debug(t.message,t)
                ctx.response().statusCode = 500;
                clientEventsManager.writeln(sessionId, "ERROR: ${t.message}");
            }
            // use the same redirection technique as gui to allow download file
            // o mirar metodo "sendfile"
            ctx.response().end();
        });

        router.get("/rest/file/:id").handler { ctx ->
            val id = ctx.request().getParam("id")
            vertx.eventBus().send<String>(AppController.Verticles.FILESERVER.address,
                    JsonObject().put("cmd","name").put("id",id),
                    { result ->
                        vertx.eventBus().send<ByteArray>(AppController.Verticles.FILESERVER.address,
                                JsonObject().put("cmd","get").put("id",id),
                                { fileResult ->
                                    val fileBytes = fileResult.result().body()
                                    ctx.response().putHeader("Content-type","octet-stream")
                                    ctx.response().putHeader("Content-Disposition","attachment; fileName=${result.result().body()}")
                                    ctx.response().putHeader("Content-Length", fileBytes.size.toString())
                                    ctx.response().write(Buffer.buffer(fileBytes))
                                    ctx.response().end()
                                })
                    })

        }
    }

}
