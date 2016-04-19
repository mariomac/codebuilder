package info.macias.codebuilder.auth

import info.macias.codebuilder.auth.impl.MemoryUserDao
import info.macias.codebuilder.auth.impl.Sha256HexDigest
import info.macias.codebuilder.auth.msg.CredentialsCall
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull


class TestAuth {
    @Ignore    @Test
    fun testSha256B64Digest() {
        assertEquals("WRlcbFQcgwfx2i0edo1vIoDJhN8hetX0xkw1QrBBEaQ=", Sha256HexDigest().digest("mario"));
        assertEquals("kZgVmoF5zjl10dRe2UtArNl9I/Ppbqnu4krGDOAXvJA=", Sha256HexDigest().digest("Cool Things Would Happen"));
    }

    @Ignore @Test
    fun testUserDao() {
        val userDao = MemoryUserDao()
        userDao.add(User("john","cipheredPassword"))
        userDao.add(User("peter","cipheredPassword",apiKey = "apikey", apiKeyExpiration = 12345))
        userDao.add(User("maria","ciphered"))
        assertEquals(4,userDao.count()) // considers the default user of MemoryUserDao
        assertEquals("cipheredPassword", userDao.get("john")!!.cipheredPassword)
        assertEquals("maria",userDao.get("maria")!!.name)
        assertEquals("apikey",userDao.get("peter")!!.apiKey)
        assertEquals(12345,userDao.get("peter")!!.apiKeyExpiration)

        assertFailsWith(UserException::class) {
            userDao.update(User("lkjsdaf","kdjfs"))
        }
        assertFailsWith(UserException::class) {
            userDao.add(User("maria","kdjfs"))
        }
    }
    @Ignore
    @Test
    fun testBasicCredentialsManager() {
        val cm = CredentialsManager(MemoryUserDao(), Sha256HexDigest())
        val mario = assertNotNull(cm.generateUser("mario","myPassword"))

        assertFailsWith(UserException::class) {
            cm.generateUser("mario","otherPasswordButSameUser")
        }
        assertNull(mario.apiKey)
        assertNull(cm.checkUserPwd("mario","otherPasswordButSameUser"))
        assertNull(mario.apiKey)
        assertNull(cm.checkKey("slkdf"))
        val user = assertNotNull(cm.checkUserPwd("mario", "myPassword"))
        assertNotNull(cm.checkKey(user.apiKey!!))
        user.apiKeyExpiration = 0
        assertNull(cm.checkKey(user.apiKey!!))
    }
    @Ignore
    @Test
    fun testCredentialsCallMsg() {
        val msg = CredentialsCall.fromJsonString("{\"username\":\"user\",\"password\":\"pwd\"}")
        assertEquals("user",msg.username)
        assertEquals("pwd",msg.plainPassword)
        assertFailsWith(Throwable::class) {
            CredentialsCall.fromJsonString("{\"usernadme\":\"user\",\"password\":\"pwd\"}")
        }
        assertFailsWith(Throwable::class) {
            CredentialsCall.fromJsonString("{\"username\":\"user\",\"password\":\"pwd\"")
        }
        val msg2 = CredentialsCall.fromJsonString("{\"username\":\"user\"}")
        assertEquals("user",msg2.username)
        assertNull(msg.plainPassword)
    }

}