package io.johnsonlee.tracing.gradle

import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.GZIPOutputStream
import kotlin.test.assertEquals

class GzipTest {

    @Test
    fun `gzip json and base64`() {
        val json = javaClass.getResourceAsStream("/trivial.json")!!.use {
            it.readBytes().toString(Charsets.UTF_8)
        }
        val encoded = ByteArrayOutputStream().use { out ->
            GZIPOutputStream(Base64.getEncoder().withoutPadding().wrap(out), 1024).use { gzip ->
                gzip.write(json.toByteArray(Charsets.UTF_8))
                gzip.flush()
            }
            out.toByteArray().toString(Charsets.UTF_8)
        }
        println(encoded)
    }

}