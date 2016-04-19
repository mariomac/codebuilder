package info.macias.codebuilder.auth

interface UserDao {
    fun count():Long
    fun forKey(key:String): User?
    fun get(username:String): User?
    @Throws(UserException::class)
    fun update(user: User)
    @Throws(UserException::class)
    fun add(user: User)
}