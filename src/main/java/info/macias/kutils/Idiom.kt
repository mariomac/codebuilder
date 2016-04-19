package info.macias.kutils

/**
 * Created by mmacias on 15/4/16.
 */

inline fun <T : AutoCloseable, R> trywr(closeable: T, block: (T) -> R): R {
    var currentThrowable: java.lang.Throwable? = null
    try {
        return block(closeable)
    } catch (throwable: Throwable) {
        currentThrowable = throwable as java.lang.Throwable
        throw throwable
    } finally {
        if (currentThrowable != null) {
            try {
                closeable.close()
            } catch (throwable: Throwable) {
                currentThrowable.addSuppressed(throwable)
            }
        } else {
            closeable.close()
        }
    }
}