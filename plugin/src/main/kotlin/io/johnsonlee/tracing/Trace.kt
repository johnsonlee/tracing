package io.johnsonlee.tracing

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.johnsonlee.tracing.event.Async
import io.johnsonlee.tracing.event.Complete
import io.johnsonlee.tracing.event.CompleteEvent
import io.johnsonlee.tracing.event.Counter
import io.johnsonlee.tracing.event.Duration
import io.johnsonlee.tracing.event.DurationEvent
import io.johnsonlee.tracing.event.Flow
import io.johnsonlee.tracing.event.Instance
import io.johnsonlee.tracing.event.Sample
import io.johnsonlee.tracing.event.TraceEvent
import io.johnsonlee.tracing.mixin.AsyncMixin
import io.johnsonlee.tracing.mixin.CompleteMixin
import io.johnsonlee.tracing.mixin.CounterMixin
import io.johnsonlee.tracing.mixin.DurationMixin
import io.johnsonlee.tracing.mixin.FlowMixin
import io.johnsonlee.tracing.mixin.InstanceMixin
import io.johnsonlee.tracing.mixin.SampleMixin
import io.johnsonlee.tracing.mixin.TraceEventMixin
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
        .addMixIn(CompleteEvent::class.java, TraceEventMixin::class.java)
        .addMixIn(DurationEvent::class.java, TraceEventMixin::class.java)
        .addMixIn(Async::class.java, AsyncMixin::class.java)
        .addMixIn(Complete::class.java, CompleteMixin::class.java)
        .addMixIn(Counter::class.java, CounterMixin::class.java)
        .addMixIn(Duration::class.java, DurationMixin::class.java)
        .addMixIn(Flow::class.java, FlowMixin::class.java)
        .addMixIn(Instance::class.java, InstanceMixin::class.java)
        .addMixIn(Sample::class.java, SampleMixin::class.java)

    private val events = CopyOnWriteArrayList<TraceEvent>()

    fun <R> trace(
            name: String,
            category: String,
            args: Map<String, Any?> = emptyMap(),
            block: () -> R
    ): R {
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
                ts = ts(),
                tts = tts(),
                pid = pid(),
                tid = tid(),
                args = args
        )
    }

    fun end(name: String, category: String, args: Map<String, Any?> = emptyMap()) = apply {
        events += DurationEvent(
                name = name,
                category = category,
                phase = Duration.END,
                ts = ts(),
                tts = tts(),
                pid = pid(),
                tid = tid(),
                args = args
        )
    }

    fun event(event: TraceEvent) = apply {
        events += event
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
