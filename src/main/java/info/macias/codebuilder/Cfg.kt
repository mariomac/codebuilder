package info.macias.codebuilder

import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 * Created by mmacias on 15/4/16.
 */
object Cfg {
    internal val log = LoggerFactory.getLogger(Cfg::class.java)
    val tmpFolder : String
    val uploadsFolder: String
    val m2Home : String
    val buildTimeout : Long
    init {
        val properties = Properties();
        properties.load(Cfg.javaClass.getResourceAsStream("/config.properties"))
        if(System.getProperty("config") != null) {
            properties.load(FileInputStream(System.getProperty("config")))
        }
        tmpFolder = properties.getProperty("tmp.folder", System.getProperty("java.io.tmpdir"))
        log.info("Temporary folder: $tmpFolder")
        uploadsFolder = properties.getProperty("uploads.folder", "${tmpFolder}file-uploads")
        log.info("Uploads folder: $uploadsFolder")
        m2Home = properties.getProperty("m2.home")
        buildTimeout = properties.getProperty("build.timeout","60000").toLong()
    }
}