package io.johnsonlee.tracing.gradle

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.johnsonlee.tracing.event.TraceEvent
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream
import java.io.Serializable
import java.util.Base64
import java.util.zip.GZIPOutputStream

object TraceGenerator {

    fun generate(out: OutputStream, title: String, events: Iterable<TraceEvent>) {
        generate(PrintStream(out, true), title, events)
    }

    fun generate(out: PrintStream, title: String, events: Iterable<TraceEvent>) {
        javaClass.getResourceAsStream("/prefix.html")!!.use { prefix ->
            prefix.copyTo(out)
            out.flush()
        }

        out.writeTitle(title)
        out.writeViewerData(events)

        javaClass.getResourceAsStream("/suffix.html")!!.use { suffix ->
            suffix.copyTo(out)
            out.flush()
        }
    }

}

private val objectMapper = jacksonObjectMapper()

internal fun <M : Serializable> PrintStream.writeViewerData(data: Iterable<M>) {
    println("<script type=\"text/javascript\">")
    println("const viewerData = ${objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data)};")
    println("</script>")
    println("<script id=\"viewer-data\" type=\"text/plain\">")
    // json => gzip => base64
    val encoded = ByteArrayOutputStream().use { out ->
        Base64.getEncoder().withoutPadding().wrap(out).also { base64 ->
            GZIPOutputStream(base64, 1024).also { gzip ->
                objectMapper.writeValue(gzip, data)
            }.flush()
        }.flush()
        out.toString("UTF-8")
    }
    println(encoded)
    println("</script>")
}

internal fun PrintStream.writeTitle(title: String) {
    println("<title>${title}</title>")
}
