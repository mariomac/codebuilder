package info.macias.codebuilder.scratch

import org.apache.maven.cli.MavenCli
import org.junit.Ignore
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
class TestPipe {
    @Ignore @Test
    fun testPiping() {

        val output = PipedOutputStream()
        val input = PipedInputStream(output)
        val print = PrintStream(output)

        print.println("Que pasa nen")
        print.close()

        var sb = StringBuilder()
        var data = input.read()
        while(data >= 0) {
            print(data.toChar())
            sb.append(data.toChar())
            if(data.toChar() == '\n') {
                print(sb.toString())
            }
            data = input.read()
        }
        assertEquals("Que pasa nen\n", sb.toString())
    }
}