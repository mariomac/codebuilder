package info.macias.codebuilder.zip

import info.macias.kutils.byteArrayToInt
import info.macias.kutils.intToByteArray
import org.junit.Test
import kotlin.test.assertEquals

class TestByteUtils {
    @Test
    fun test() {
        val tests = arrayOf(0,1,-1,128,239084,903285,90238503)
        for(i in tests) {
            assertEquals(i, byteArrayToInt(intToByteArray(i)))
        }
    }

}