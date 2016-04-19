package info.macias.codebuilder.auth

data class User(
        var name:String,
        var cipheredPassword:String,
        var salt:ByteArray= ByteArray(0),
        var apiKey:String?=null,
        var apiKeyExpiration:Long=0) {
}