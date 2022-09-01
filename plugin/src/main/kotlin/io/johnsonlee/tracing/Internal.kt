package io.johnsonlee.tracing

import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

@JvmSynthetic
internal fun ts(): Long = TimeUnit.NANOSECONDS.toMicros(System.nanoTime())

@JvmSynthetic
internal fun tts(): Long = TimeUnit.NANOSECONDS.toMicros(ManagementFactory.getThreadMXBean().currentThreadCpuTime)

@JvmSynthetic
internal fun pid(): Long = ManagementFactory.getRuntimeMXBean().name.substringBefore('@').toLongOrNull() ?: 0

@JvmSynthetic
internal fun tid(): Long = Thread.currentThread().id