package io.johnsonlee.tracing.mixin

import io.johnsonlee.tracing.Trace
import org.junit.Test
import java.io.ByteArrayOutputStream
import kotlin.test.assertTrue

class TraceTest {

    @Test
    fun `check jackson mixin`() {
        val out = ByteArrayOutputStream()
        Trace.begin("test", "trace").dump("test", out)
        assertTrue(out.size() > 0)
    }

}