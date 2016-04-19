package info.macias.codebuilder.auth.msg

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.vertx.core.json.JsonObject

data class CredentialsCall(var username:String?=null, var plainPassword:String?=null, var apikey:String?=null) {

    companion object {
        val OK = 0
        val NULL_USER_PWD = -1
        val NULL_APIKEY = -2
        val INVALID_USER_PWD = -3
        val INVALID_APIKEY = -4
        fun fromJsonObject(json : JsonObject): CredentialsCall = CredentialsCall(json.getString("username"), json.getString("password"), json.getString("apikey"))
        fun fromJsonString(json : String): CredentialsCall = jacksonObjectMapper().readValue(json, CredentialsCall::class.java)
        fun toJson(method: CredentialsCall): String = jacksonObjectMapper().writeValueAsString(method)
        fun toJsonObject(method: CredentialsCall): JsonObject {
            val json = JsonObject();
            if(method.username != null) json.put("username",method.username)
            if(method.plainPassword != null) json.put("password",method.plainPassword)
            if(method.apikey != null) json.put("apikey",method.apikey);
            return json
        }
    }
}

