package info.macias.codebuilder.http

import info.macias.codebuilder.auth.DbAuthProviderClient
import info.macias.codebuilder.auth.msg.CredentialsCall
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.RedirectAuthHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.sstore.LocalSessionStore
import io.vertx.ext.web.templ.HandlebarsTemplateEngine
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templateresolver.ITemplateResolver
import java.util.*

/**
 * Created by mmacias on 30/3/16.
 */
internal class GuiEndpoint() {
    val handleBars : HandlebarsTemplateEngine
    init {
        handleBars= HandlebarsTemplateEngine.create();
        val templateResolvers = HashSet<ITemplateResolver>()
        // Load handlebar templates from classpath
        templateResolvers.add(ClassLoaderTemplateResolver())
    }

    fun register(router : Router, vertx : Vertx) {
        // Convenience method, just for comfortability
        router.route("/gui").handler { redirect(it,"/gui/index") }

        // Static assets handling
        router.route("/static/*").method(HttpMethod.GET).handler(StaticHandler.create())

        // Session&cookies handling for GUI
        router.route("/gui/*").handler(io.vertx.ext.web.handler.CookieHandler.create());
        router.route("/gui/*").handler(SessionHandler.create(LocalSessionStore.create(vertx))
                .setNagHttps(false).setCookieSecureFlag(false)); // just for development purposes, don't bother me with warnings

        // Authentication
        val authProvider = DbAuthProviderClient(vertx.eventBus())
        router.route("/gui/*").handler(io.vertx.ext.web.handler.UserSessionHandler.create(authProvider));
        router.route("/gui/*").handler(RedirectAuthHandler.create(authProvider,"/login"));
        router.route("/login").method(HttpMethod.GET).handler { ctx -> renderTemplate(ctx,"templates/login.hbs") }

        router.route("/login").method(HttpMethod.POST).handler({ ctx ->
            if (ctx.bodyAsString == null) {
                // bad request
                ctx.fail(400);
            } else {
                try {
                    var credentials = CredentialsCall(ctx.request().getFormAttribute("username"), ctx.request().getFormAttribute("password"))
                    authProvider.authenticate(CredentialsCall.toJsonObject(credentials), { login ->
                        if (login.failed()) {
                            ctx.put("error", login.cause())
                            renderTemplate(ctx, "templates/login.hbs")
                        } else {
                            println("login ok")
                            ctx.setUser(login.result());
                            redirect(ctx,"/gui/index")
                        }
                    })
                } catch(e:Throwable) {
                    ctx.fail(500)
                }
            }
        })

        router.route("/logout").handler({ctx ->
            ctx.clearUser();
            ctx.setUser(null)
            ctx.cookies().clear()
            ctx.session()?.destroy();
            redirect(ctx, "/login")
        })

        // Main page
        router.route("/gui/index").handler({ ctx ->
            ctx.put("username", ctx.user().principal().getString("name"))
            ctx.put("apiKey", ctx.user().principal().getString("apiKey"))
            ctx.put("sessionId", ctx.session().id())
            renderTemplate(ctx, "templates/index.hbs")
        })
    }

    /**** Helper functions and classes ****/

    /**
     * Helper function for page redirection
     */
    private fun redirect(ctx: RoutingContext, path: String) {
        ctx.response().headers().add("Location",path)
        ctx.response().statusCode = 301
        ctx.reroute(path)
    }

    /**
     * Renders a handlebars template
     */
    private fun renderTemplate(ctx: RoutingContext, file:String) {
        handleBars.render(ctx, file,{ res ->
            if (res.succeeded()) {
                ctx.response().end(res.result())
            } else {
                ctx.fail(res.cause())
            }
        })
    }
}