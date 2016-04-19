package info.macias.codebuilder.console

import info.macias.sse.EventTarget
import info.macias.sse.vertx3.VertxEventTarget
import io.vertx.core.http.HttpServerRequest
import java.util.*

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
class ConsoleManager {
    // Todo: use a list of event target to see multiple client output
    val users = HashMap<String, EventTarget>()

    fun listen(userid:String, request:HttpServerRequest) {
        synchronized(users) {
            users.put(userid, VertxEventTarget(request).ok())
        }
    }

    fun writeln(userid:String, text:String) {
        synchronized(users) {
            users.get(userid)?.send("message",text)
        }
    }
}