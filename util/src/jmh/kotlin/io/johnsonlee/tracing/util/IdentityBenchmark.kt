package io.johnsonlee.tracing.util

import io.johnsonlee.tracing.util.LooperUtil.identity
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 2, jvmArgs = ["-Xms2G", "-Xmx2G"])
@State(Scope.Benchmark)
@Measurement(iterations = 5)
@Warmup(iterations = 2)
open class IdentityBenchmark {

    @Benchmark
    fun identity_with_handler() {
        identity(">>>>> Dispatching to Handler (android.app.ActivityThread\$H) {33e0fae} null: 1")
    }

    @Benchmark
    fun identity_with_handler_and_callback() {
        identity(">>>>> Dispatching to Handler (android.app.ActivityThread\$H) {33e0fae} android.view.ViewRootImpl$4@33e0fae: 1")
    }

    @Benchmark
    fun identity_with_coroutine_main_dispatcher() {
        identity(">>>>> Dispatching to Handler (android.app.ActivityThread\$H) {33e0fae} DispatchedContinuation[Dispatchers.Main, Continuation at com.example.Example@123456]: 1")
    }


    @Benchmark
    fun identity_with_coroutine_immediate_dispatcher() {
        identity(">>>>> Dispatching to Handler (android.app.ActivityThread\$H) {33e0fae} DispatchedContinuation[Dispatchers.Main.immediate, Continuation at com.example.Example@123456]: 1")
    }

}