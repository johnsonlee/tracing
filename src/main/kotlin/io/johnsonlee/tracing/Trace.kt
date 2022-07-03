package io.johnsonlee.tracing

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.johnsonlee.tracing.event.Duration
import io.johnsonlee.tracing.event.DurationEvent
import io.johnsonlee.tracing.event.TraceEvent
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.io.PrintStream
import java.io.Serializable
import java.util.Base64
import java.util.concurrent.CopyOnWriteArrayList
import java.util.zip.GZIPOutputStream

object Trace {

    private val objectMapper = jacksonObjectMapper()

    private val events = CopyOnWriteArrayList<TraceEvent>()

    fun <R> trace(
            name: String,
            category: String,
            args: Map<String, Any?> = emptyMap(),
            block: () -> R
    ): R{
        begin(name, category, args)
        val result = block()
        end(name, category, args)
        return result
    }

    fun begin(name: String, category: String, args: Map<String, Any?> = emptyMap()) = apply {
        events += DurationEvent(
                name = name,
                category = category,
                phase = Duration.BEGIN,
                args = args
        )
    }

    fun end(name: String, category: String, args: Map<String, Any?> = emptyMap()) = apply {
        events += DurationEvent(
                name = name,
                category = category,
                phase = Duration.END,
                args = args
        )
    }

    fun dump(title: String, output: File) = output.outputStream().buffered().use { out ->
        dump(title, out)
    }

    fun dump(title: String, out: () -> OutputStream) {
        dump(title, PrintStream(out(), true))
    }

    fun dump(title: String, out: OutputStream) {
        dump(title, PrintStream(out, true))
    }

    fun dump(title: String, out: PrintStream) {
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

        events.clear()
    }

    private fun <M : Serializable> PrintStream.writeViewerData(data: Iterable<M>) {
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

    private fun PrintStream.writeTitle(title: String) {
        println("<title>${title}</title>")
    }
}
