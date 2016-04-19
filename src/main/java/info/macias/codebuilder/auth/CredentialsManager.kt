package info.macias.codebuilder.auth

import java.security.SecureRandom

class CredentialsManager(val userDao: UserDao, val digester : Digester) {

    private val keyValidityPeriod = 3600 * 10000 // 1 hour
    /**
     * Checks the username/password access credentials
     * @param username
     * @param plainPwd the plain password, as introduced by the user
     * @return If the user/pwd combination is wrong, returns null.
     *         If not, returns the user with the updated Random API Key that will be used for REST-based services
     */
    fun checkUserPwd(username:String, plainPwd:String):User? {
        val u = userDao.get(username)
        if (u == null || u.cipheredPassword != digester.digest(plainPwd,u.salt)) {
            return null;
        } else {
            val now = System.currentTimeMillis()
            if(u.apiKey == null || u.apiKeyExpiration < now) {
                generateKeyForUser(u)
            } else {
                u.apiKeyExpiration = System.currentTimeMillis() + keyValidityPeriod
            }
            userDao.update(u)
            return u
        }
    }

    /**
     * Checks the validity of an API key. It also increases the expiration time of the
     * key as now + 1 hour
     *
     * @param key The API key
     * @eturn the user to which the key belongs, or null if it is an invalid key
     */
    fun checkKey(apiKey:String):User? {
        val u = userDao.forKey(apiKey)
        if(u == null) {
            return null;
        } else {
            return if(u!!.apiKeyExpiration > System.currentTimeMillis()) u else null;
        }
    }

    private val rnd = SecureRandom()
    /**
     * It adds the user to the database
     */
    @Throws(UserException::class)
    fun generateUser(username: String, plainPwd: String):User {
        if(userDao.get(username) != null) throw UserException("${username} already exists")
        val salt = ByteArray(8)
        rnd.nextBytes(salt)
        val u = User(username,digester.digest(plainPwd, salt),salt)
        userDao.add(u)
        return u
    }

    private fun generateKeyForUser(user: User) {
        val key = ByteArray(32)
        rnd.nextBytes(key)
        user.apiKey = digester.encode(key)
        user.apiKeyExpiration = System.currentTimeMillis() + keyValidityPeriod
    }
}

interface Digester {
    fun digest(input:String, salt:ByteArray = ByteArray(0)) : String
    fun encode(bytes:ByteArray) : String
}

class UserException(message:String?=null,cause:Throwable?=null) : Exception(message,cause) {

}
