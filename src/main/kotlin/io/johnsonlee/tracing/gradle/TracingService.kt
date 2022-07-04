package io.johnsonlee.tracing.gradle

import io.johnsonlee.tracing.Trace
import io.johnsonlee.tracing.event.CompleteEvent
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener
import org.gradle.tooling.events.configuration.ProjectConfigurationFinishEvent
import org.gradle.tooling.events.task.TaskFailureResult
import org.gradle.tooling.events.task.TaskFinishEvent
import org.gradle.tooling.events.task.TaskSkippedResult
import org.gradle.tooling.events.task.TaskSuccessResult
import org.gradle.tooling.events.test.TestFailureResult
import org.gradle.tooling.events.test.TestFinishEvent
import org.gradle.tooling.events.test.TestSkippedResult
import org.gradle.tooling.events.test.TestSuccessResult
import org.gradle.tooling.events.transform.TransformFinishEvent
import org.gradle.tooling.events.work.WorkItemFinishEvent
import java.io.File
import java.util.concurrent.TimeUnit

@Suppress("UnstableApiUsage")
abstract class TracingService : BuildService<TracingService.Parameters>, AutoCloseable, OperationCompletionListener {

    interface Parameters : BuildServiceParameters {
        val pid: Property<Long>
        val startTime: Property<Long>
        val project: Property<String>
        val cmdline: Property<String>
        val destination: DirectoryProperty
    }

    override fun onFinish(event: FinishEvent) {
        when (event) {
            is ProjectConfigurationFinishEvent -> {
                Trace.event(CompleteEvent(
                        name = event.displayName,
                        category = CATEGORY_CONFIGURATION,
                        duration = event.duration,
                        ts = event.ts,
                        tts = event.tts,
                        pid = parameters.pid.get(),
                        tid = 0L
                ))
            }
            is TaskFinishEvent -> when (val result = event.result) {
                is TaskSuccessResult -> {
                    Trace.event(CompleteEvent(
                            name = event.displayName,
                            category = CATEGORY_EXECUTION,
                            duration = event.duration,
                            ts = event.ts,
                            tts = event.tts,
                            pid = parameters.pid.get(),
                            tid = 0L,
                            args = mapOf(
                                    "incremental" to result.isIncremental,
                                    "fromCache" to result.isFromCache,
                                    "upToDate" to result.isUpToDate
                            )
                    ))
                }
                is TaskSkippedResult -> {
                    Trace.event(CompleteEvent(
                            name = event.displayName,
                            category = CATEGORY_EXECUTION,
                            duration = event.duration,
                            ts = event.ts,
                            tts = event.tts,
                            pid = parameters.pid.get(),
                            tid = 0L,
                            args = mapOf(
                                    "skipMessage" to result.skipMessage
                            )
                    ))
                }
                is TaskFailureResult -> {
                    Trace.event(CompleteEvent(
                            name = event.displayName,
                            category = CATEGORY_EXECUTION,
                            duration = event.duration,
                            ts = event.ts,
                            tts = event.tts,
                            pid = parameters.pid.get(),
                            tid = 0L,
                            args = mapOf(
                                    "incremental" to result.isIncremental,
                                    "failures" to result.failures
                            )
                    ))
                }

            }
            is TestFinishEvent -> when (val result = event.result) {
                is TestSuccessResult -> {
                    Trace.event(CompleteEvent(
                            name = event.displayName,
                            category = CATEGORY_TEST,
                            duration = event.duration,
                            ts = event.ts,
                            tts = event.tts,
                            pid = parameters.pid.get(),
                            tid = 0L,
                            args = mapOf("success" to true)
                    ))
                }
                is TestFailureResult -> {
                    Trace.event(CompleteEvent(
                            name = event.displayName,
                            category = CATEGORY_TEST,
                            duration = event.duration,
                            ts = event.ts,
                            tts = event.tts,
                            pid = parameters.pid.get(),
                            tid = 0L,
                            args = mapOf("failures" to result.failures)
                    ))
                }
                is TestSkippedResult -> {
                    Trace.event(CompleteEvent(
                            name = event.displayName,
                            category = CATEGORY_TEST,
                            duration = event.duration,
                            ts = event.ts,
                            tts = event.tts,
                            pid = parameters.pid.get(),
                            tid = 0L,
                            args = mapOf("skipped" to true)
                    ))
                }
            }
            is TransformFinishEvent -> {
                Trace.event(CompleteEvent(
                        name = event.displayName,
                        category = CATEGORY_EXECUTION,
                        duration = event.duration,
                        ts = event.ts,
                        tts = event.tts,
                        pid = parameters.pid.get(),
                        tid = 0L
                ))
            }
            is WorkItemFinishEvent -> {
                Trace.event(CompleteEvent(
                        name = event.displayName,
                        category = CATEGORY_EXECUTION,
                        duration = event.duration,
                        ts = event.ts,
                        tts = event.tts,
                        pid = parameters.pid.get(),
                        tid = 0L
                ))
            }
            else -> {
                Trace.event(CompleteEvent(
                        name = event.displayName,
                        category = CATEGORY_UNKNOWN,
                        duration = event.duration,
                        ts = event.ts,
                        tts = event.tts,
                        pid = parameters.pid.get(),
                        tid = 0L
                ))
            }
        }
    }

    override fun close() {
        Trace.dump(parameters.project.get(), File(parameters.destination.asFile.get(), "trace.html"))
    }

}

private val FinishEvent.duration: Long
    get() = TimeUnit.MILLISECONDS.toMicros(result.endTime - result.startTime)

private val FinishEvent.ts: Long
    get() = TimeUnit.MILLISECONDS.toMicros(result.startTime)

private val FinishEvent.tts: Long
    get() = TimeUnit.MILLISECONDS.toMicros(result.startTime)