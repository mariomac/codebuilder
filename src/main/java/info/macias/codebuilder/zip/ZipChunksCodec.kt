package info.macias.codebuilder.zip

import info.macias.kutils.byteArrayToInt
import info.macias.kutils.intToByteArray
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec

/**
 * The format of each zipchunks list is
 * 4 bytes --> Zip file size
 * rest --> the chunk
 * @author Mario Macias (http://github.com/mariomac)
 */
class ZipChunksCodec : MessageCodec<List<ByteArray>,ByteArray> {
    companion object {
        val codecName = "ZipChunksCodec";
    }

    override fun name(): String? = codecName

    override fun encodeToWire(buffer: Buffer?, s: List<ByteArray>?) {
        if(s == null) {
            buffer?.appendByte(0)?.appendByte(0)?.appendByte(0)?.appendByte(0)
        } else {
            var size:Int = 0
            for(b in s) {
                size += b.size
            }
            println("EncodeToWire size: $size")
            val sizeb = intToByteArray(size)
            buffer?.appendByte(sizeb[0])
            buffer?.appendByte(sizeb[1])
            buffer?.appendByte(sizeb[2])
            buffer?.appendByte(sizeb[3])

            for(b in s) {
                buffer?.appendBytes(b)
            }
        }
    }

    override fun transform(s: List<ByteArray>?): ByteArray? {
        var size = 0
        if(s == null) {
            return ByteArray(0)
        } else {
            for(b in s) {
                size += b.size
            }
            println("Transform size: $size")
            val bytes = ByteArray(size+4)
            val sizeb = intToByteArray(size)
            bytes[0]=sizeb[0]
            bytes[1]=sizeb[1]
            bytes[2]=sizeb[2]
            bytes[3]=sizeb[3]
            var idx = 4
            for(b in s) {
                System.arraycopy(b,0,bytes,idx,b.size)
                idx += b.size
            }
            return bytes
        }
    }

    override fun systemCodecID(): Byte {
        return -1 //should always return -1 for user codecs
    }

    override fun decodeFromWire(pos: Int, buffer: Buffer?): ByteArray? {
        val sizeb = buffer?.getBytes(pos,pos+4)!!
        val size:Int = byteArrayToInt(sizeb)
        println("DecodeFromWire size: $size")
        return buffer?.getBytes(pos+4,pos+4+size)
    }
}