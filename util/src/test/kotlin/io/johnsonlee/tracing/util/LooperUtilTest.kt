package io.johnsonlee.tracing.util

import io.johnsonlee.tracing.util.LooperUtil.buildTraceLabel
import io.johnsonlee.tracing.util.LooperUtil.findCallback
import io.johnsonlee.tracing.util.LooperUtil.findWhat
import org.junit.Test
import kotlin.test.assertEquals

class LooperUtilTest {

    @Test
    fun `build trace label with handler`() {
        val label = buildTraceLabel(">>>>> Dispatching to Handler (android.app.ActivityThread\$H) {33e0fae} null: 1")
        assertEquals("android.app.ActivityThread\$H", label)
    }

    @Test
    fun `build trace label with handler and callback`() {
        val label = buildTraceLabel(">>>>> Dispatching to Handler (android.app.ActivityThread\$H) {33e0fae} android.view.ViewRootImpl$4@33e0fae: 1")
        assertEquals("android.view.ViewRootImpl$4", label)
    }

    @Test
    fun `build trace label with obfuscated handler and callback`() {
        val label = buildTraceLabel(">>>>> Dispatching to Handler (android.app.ActivityThread\$H) {33e0fae} a.b.c@33e0fae: 1")
        assertEquals("a.b.c", label)
    }

    @Test
    fun `build trace label with coroutine main dispatcher`() {
        val label = buildTraceLabel(">>>>> Dispatching to Handler (android.app.ActivityThread\$H) {33e0fae} DispatchedContinuation[Dispatchers.Main, Continuation at com.example.Example@123456]: 1")
        assertEquals("com.example.Example", label)
    }

    @Test
    fun `build trace label with coroutine immediate dispatcher`() {
        val label = buildTraceLabel(">>>>> Dispatching to Handler (android.app.ActivityThread\$H) {33e0fae} DispatchedContinuation[Dispatchers.Main.immediate, Continuation at com.example.Example@123456]: 1")
        assertEquals("com.example.Example", label)
    }

    @Test
    fun `get what from loop enter message`() {
        assertEquals(1, findWhat(">>>>> Dispatching to Handler (android.app.ActivityThread\$H) {33e0fae} null: 1"))
        assertEquals(2, findWhat(">>>>> Dispatching to Handler (android.app.ActivityThread\$H) {33e0fae} null: 2"))
        assertEquals(3, findWhat(">>>>> Dispatching to Handler (android.app.ActivityThread\$H) {33e0fae} null: 3"))
    }

    @Test
    fun `get what from loop exit message`() {
        assertEquals(0, findWhat("<<<<< Finished to Handler (android.app.ActivityThread\$H) {33e0fae} null"))
    }

    @Test
    fun `find coroutine label from message 1`() {
        assertEquals("com.example.Example", findCallback("<<<<< Finished to Handler (android.app.ActivityThread\$H) {123456} DispatchedContinuation[Dispatchers.Default, Continuation at com.example.Example@123456]"))
    }

    @Test
    fun `find coroutine label from message 2`() {
        assertEquals("com.example.Example", findCallback("<<<<< Finished to Handler (android.app.ActivityThread\$H) {123456} DispatchedContinuation[Dispatcher(TestCoroutineContext@123456), Continuation at com.example.Example@123456]"))
    }

    @Test
    fun `find coroutine label from message 3`() {
        assertEquals("com.example.Example", findCallback("<<<<< Finished to Handler (android.app.ActivityThread\$H) {123456} DispatchedContinuation[DefaultDispatcher@123456[Pool Size {core = 1, max = 2}, Worker States {CPU = 8, blocking = 0, parked = 0, dormant = 0, terminated = 0}, running workers queues = 1, global CPU queue size = 1, global blocking queue size = 1, Control State {created workers = 0, blocking tasks = 0, CPUs acquired = 0}], Continuation at com.example.Example@123456]"))
    }

}