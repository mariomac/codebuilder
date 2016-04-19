package info.macias.kutils

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
inline fun intToByteArray(num:Int):ByteArray {
    val bytes = ByteArray(4)
    bytes[0]=(num and 0xff).toByte()
    bytes[1]=((num ushr(8)) and 0xff).toByte()
    bytes[2]=((num ushr(16)) and 0xff).toByte()
    bytes[3]=((num ushr(24)) and 0xff).toByte()
    return bytes
}

inline fun byteArrayToInt(numb:ByteArray):Int {
    return byteArrayToInt(numb, 0)
}

inline fun byteArrayToInt(numb:ByteArray, from:Int):Int {
    return (numb[from+0].toInt() and 0xff) or
            ((numb[from+1].toInt() and 0xff) shl(8)) or
            ((numb[from+2].toInt() and 0xff) shl(16)) or
            ((numb[from+3].toInt() and 0xff) shl(24))
}