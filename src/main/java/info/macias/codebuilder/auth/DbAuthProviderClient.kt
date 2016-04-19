package info.macias.codebuilder.auth

import info.macias.codebuilder.AppController
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.auth.User

internal class DbAuthProviderClient(val eventBus: EventBus) : AuthProvider {
    override fun authenticate(credentials: JsonObject?, loginHandler: Handler<AsyncResult<User>>?) {
        eventBus.send<String>(
                AppController.Verticles.CREDENTIALS.address,
                credentials,
                { result ->
                    if(result.succeeded()) {
                        val returnedUser = DbCredentialsUser(JsonObject(result.result().body()))
                        loginHandler!!.handle(Future.succeededFuture(returnedUser))
                    } else {
                        loginHandler!!.handle(Future.failedFuture(result.cause()))
                    }
                })
    }

    /**
     * Users that holds extra information, such as the API key
     */
    class DbCredentialsUser(val dbCredentials: JsonObject) : User {
        override fun isAuthorised(p0: String?, p1: Handler<AsyncResult<Boolean>>?): User? {
            return this;
        }

        override fun clearCache(): User? {
            return this;
        }

        override fun setAuthProvider(p0: AuthProvider?) {
        }

        override fun principal(): JsonObject? {
            return dbCredentials;
        }
    }

}