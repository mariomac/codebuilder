package info.macias.codebuilder.auth.impl

import info.macias.codebuilder.auth.User
import info.macias.codebuilder.auth.UserDao
import info.macias.codebuilder.auth.UserException
import java.util.*


class MemoryUserDao: UserDao {

    private val users = HashMap<String,User>()

    override fun count(): Long = users.values.size.toLong()

    init {
        users.put("user",User("user","04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb")) //password: user
    }
    override fun get(username: String): User?
            = users.get(username)

    override fun update(user: User) {
        if(users.containsKey(user.name)) {
            users.put(user.name, user)
        } else {
            throw UserException("User ${user.name} does not exist")
        }
    }

    override fun add(user: User) {
        if(users.containsKey(user.name)) {
            throw UserException("User ${user.name} already exist")
        } else {
            users.put(user.name, user)
        }
    }

    override fun forKey(key: String): User? {
        for(u in users.values) {
            if(u.apiKey == key) {
                return u
            }
        }
        // todo: only for debugging. Remove it
        if(key == "test") {
            val u = users.get("user")
            u!!.apiKeyExpiration = Long.MAX_VALUE;
            return u
        }
        return null
    }
}