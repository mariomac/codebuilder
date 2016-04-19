package info.macias.codebuilder.auth

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import info.macias.codebuilder.auth.msg.CredentialsCall
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
class CredentialsVerticle(val address:String, val credentialsManager: CredentialsManager) : AbstractVerticle() {
    private val logger = LoggerFactory.getLogger(CredentialsVerticle::class.java)
    override fun start() {
        val eventBus = vertx.eventBus()
        eventBus.consumer<String>(address).handler { message ->
            logger.trace("Received message from ${message.replyAddress()}")
            val credentials:CredentialsCall = CredentialsCall.fromJsonObject(message.body() as JsonObject)
            val dbUser:User? = if(credentials.apikey != null) {
                val user = credentialsManager.checkKey(credentials.apikey ?: "");
                if(user == null) {
                    message.fail(CredentialsCall.INVALID_APIKEY,"Invalid API key");
                }
                user
            } else if(credentials.username == null || credentials.plainPassword == null) {
                message.fail(CredentialsCall.NULL_USER_PWD, "Username or password cannot be null")
                null
            } else {
                val user = credentialsManager.checkUserPwd(credentials.username as String, credentials.plainPassword as String)
                if(user == null) {
                    message.fail(CredentialsCall.INVALID_USER_PWD,"Username or password incorrect")
                }
                user
            }
            if(dbUser != null) {
                message.reply(jacksonObjectMapper().writeValueAsString(dbUser))
            }
        }
    }
}