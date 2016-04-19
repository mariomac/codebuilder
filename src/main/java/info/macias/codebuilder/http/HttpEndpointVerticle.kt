package info.macias.codebuilder.http

import info.macias.codebuilder.Cfg
import io.vertx.core.AbstractVerticle
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
class HttpEndpointVerticle : AbstractVerticle() {

    override fun start() {
        val router = Router.router(vertx)

        // Allows getting body in POST methods
        router.route().handler(BodyHandler.create(Cfg.uploadsFolder))

        GuiEndpoint().register(router,vertx)
        RestEndpoint().register(router,vertx)

        // Instantiate HTTP server
        val server = vertx.createHttpServer()
                .requestHandler({ router.accept(it) })

        server.listen(System.getProperty("port", "8080").toInt())
    }


}

