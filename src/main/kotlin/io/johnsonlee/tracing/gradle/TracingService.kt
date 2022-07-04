package io.johnsonlee.tracing.gradle

import io.johnsonlee.tracing.Trace
import io.johnsonlee.tracing.event.CompleteEvent
import io.johnsonlee.tracing.event.TraceEvent
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.internal.operations.OperationIdentifier
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener
import org.gradle.tooling.events.configuration.ProjectConfigurationFinishEvent
import org.gradle.tooling.events.internal.OperationDescriptorWrapper
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
import org.gradle.tooling.internal.protocol.events.InternalTaskDescriptor
import java.io.File
import java.util.concurrent.TimeUnit

@Suppress("UnstableApiUsage")
abstract class TracingService : BuildService<TracingService.Parameters>, AutoCloseable, OperationCompletionListener {

    interface Parameters : BuildServiceParameters {
        val pid: Property<Long>
        val project: Property<String>
        val cmdline: Property<String>
        val destination: DirectoryProperty
        val tasks: MapProperty<String, Long>
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
                        pid = event.pid,
                        tid = 0L,
                        args = mapOf("id" to event.id)
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
                            pid = event.pid,
                            tid = event.tid,
                            args = mapOf(
                                    "id" to event.id,
                                    "identityPath" to event.identityPath,
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
                            pid = event.pid,
                            tid = event.tid,
                            args = mapOf(
                                    "id" to event.id,
                                    "identityPath" to event.identityPath,
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
                            pid = event.pid,
                            tid = event.tid,
                            args = mapOf(
                                    "id" to event.id,
                                    "identityPath" to event.identityPath,
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
                            pid = event.pid,
                            tid = event.tid,
                            args = mapOf(
                                    "id" to event.id,
                                    "success" to true
                            )
                    ))
                }
                is TestFailureResult -> {
                    Trace.event(CompleteEvent(
                            name = event.displayName,
                            category = CATEGORY_TEST,
                            duration = event.duration,
                            ts = event.ts,
                            tts = event.tts,
                            pid = event.pid,
                            tid = event.tid,
                            args = mapOf(
                                    "id" to event.id,
                                    "failures" to result.failures
                            )
                    ))
                }
                is TestSkippedResult -> {
                    Trace.event(CompleteEvent(
                            name = event.displayName,
                            category = CATEGORY_TEST,
                            duration = event.duration,
                            ts = event.ts,
                            tts = event.tts,
                            pid = event.pid,
                            tid = event.tid,
                            args = mapOf(
                                    "id" to event.id,
                                    "skipped" to true
                            )
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
                        pid = event.pid,
                        tid = event.tid,
                        args = mapOf("id" to event.id)
                ))
            }
            is WorkItemFinishEvent -> {
                Trace.event(CompleteEvent(
                        name = event.displayName,
                        category = CATEGORY_EXECUTION,
                        duration = event.duration,
                        ts = event.ts,
                        tts = event.tts,
                        pid = event.pid,
                        tid = event.tid,
                        args = mapOf("id" to event.id)
                ))
            }
            else -> {
                Trace.event(CompleteEvent(
                        name = event.displayName,
                        category = CATEGORY_UNKNOWN,
                        duration = event.duration,
                        ts = event.ts,
                        tts = event.tts,
                        pid = event.pid,
                        tid = event.tid,
                        args = mapOf("id" to event.id)
                ))
            }
        }
    }

    override fun close() {
        Trace.dump(parameters.project.get(), File(parameters.destination.asFile.get(), "trace.html"))
    }

    private val FinishEvent.id: Long
        get() = ((descriptor as? OperationDescriptorWrapper)?.internalOperationDescriptor?.id as? OperationIdentifier)?.id ?: 0L

    private val FinishEvent.pid: Long
        get() = parameters.pid.get()

    private val FinishEvent.tid: Long
        get() = identityPath?.let(parameters.tasks.get()::get) ?: 0L

    private val TraceEvent.threadId: Long
        get() = (args["identityPath"] as? String)?.let {
            parameters.tasks.getting(it).orNull
        } ?: 0L

    private val FinishEvent.identityPath: String?
        get() = ((descriptor as? OperationDescriptorWrapper)?.internalOperationDescriptor as? InternalTaskDescriptor)?.name

}

private val FinishEvent.duration: Long
    get() = TimeUnit.MILLISECONDS.toMicros(result.endTime - result.startTime)

private val FinishEvent.ts: Long
    get() = TimeUnit.MILLISECONDS.toMicros(result.startTime)

private val FinishEvent.tts: Long
    get() = TimeUnit.MILLISECONDS.toMicros(result.startTime)