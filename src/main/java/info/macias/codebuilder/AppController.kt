package info.macias.codebuilder

import info.macias.codebuilder.auth.CredentialsManager
import info.macias.codebuilder.auth.CredentialsVerticle
import info.macias.codebuilder.auth.impl.MemoryUserDao
import info.macias.codebuilder.auth.impl.Sha256HexDigest
import info.macias.codebuilder.builder.BuilderVerticle
import info.macias.codebuilder.console.ConsoleManager
import info.macias.codebuilder.http.HttpEndpointVerticle
import info.macias.codebuilder.zip.ZipWorkerVerticle
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Verticle
import io.vertx.core.VertxOptions
import org.slf4j.LoggerFactory

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
class AppController() : AbstractVerticle() {

    private val log = LoggerFactory.getLogger(AppController::class.java)

    override fun start() {
        log.info("Starting AppController Verticle")

        // to debug, run with system property -Dvertx.options.blockedThreadCheckInterval=922337203685477580
        Verticles.values().forEach {
            val options = DeploymentOptions().setWorker(it.worker)
            vertx.deployVerticle(it.verticle,options)
        }
    }

    override fun stop() {
        log.info("Stopping AppController Verticle")

    }

    enum class Verticles(val address: String, val verticle:Verticle, val worker : Boolean = false) {
        CREDENTIALS("Credentials", CredentialsVerticle("Credentials", CredentialsManager(MemoryUserDao(), Sha256HexDigest()))),
        HTTP("HTTP", HttpEndpointVerticle()), // for heavy drop files
        ZIP("ZipWorker", ZipWorkerVerticle("ZipWorker", Cfg.tmpFolder), worker = true),
        BUILDER("Builder", BuilderVerticle("Builder",Cfg.m2Home), worker=true )
    }
}