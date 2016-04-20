package info.macias.codebuilder.console

import info.macias.sse.EventTarget
import info.macias.sse.vertx3.VertxEventTarget
import io.vertx.core.http.HttpServerRequest
import java.net.URL
import java.util.*

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
class EventsManager {

    companion object {
        private val evMessage = "message"
        private val evFileReady = "fileReady"
    }

    // Todo: use a list of event target to see multiple client output
    val users = HashMap<String, EventTarget>()

    // todo: consider using an per-instance uuid instead of a session id
    fun listen(sessionId:String, request:HttpServerRequest) {
        synchronized(users) {
            users.put(sessionId, VertxEventTarget(request).ok())
        }
    }

    fun writeln(sessionId:String, text:String) {
        synchronized(users) {
            users.get(sessionId)?.send(evMessage,text)
        }
    }

    fun onFileReady(sessionId:String, path:String) {
        synchronized(users) {
            users.get(sessionId)?.send(evFileReady,path)
        }
    }






}