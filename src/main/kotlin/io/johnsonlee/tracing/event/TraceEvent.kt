package io.johnsonlee.tracing.event

import java.io.Serializable
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

interface TraceEvent : Serializable {
    val name: String
    val category: String
    val phase: EventPhase
    val ts: Long
    val tts: Long
    val pid: Long
    val tid: Long
    val args: Map<String, Any?>
}

fun ts(): Long = TimeUnit.NANOSECONDS.toMicros(System.nanoTime())

fun tts(): Long = TimeUnit.NANOSECONDS.toMicros(ManagementFactory.getThreadMXBean().currentThreadCpuTime)

fun pid(): Long = ManagementFactory.getRuntimeMXBean().name.substringBefore('@').toLongOrNull() ?: 0

fun tid(): Long = Thread.currentThread().id