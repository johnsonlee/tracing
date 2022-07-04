package io.johnsonlee.tracing.gradle

import io.johnsonlee.tracing.event.pid
import io.johnsonlee.tracing.event.tid
import org.gradle.api.Plugin
import org.gradle.api.internal.TaskInternal
import org.gradle.api.invocation.Gradle
import org.gradle.api.model.ObjectFactory
import org.gradle.build.event.BuildEventsListenerRegistry
import javax.inject.Inject

@Suppress("UnstableApiUsage")
class GradleTracingPlugin @Inject constructor(
        private val objectFactory: ObjectFactory,
        private val buildEventsListenerRegistry: BuildEventsListenerRegistry
) : Plugin<Gradle> {

    override fun apply(gradle: Gradle) {
        val projectDir = gradle.startParameter.projectDir ?: gradle.startParameter.currentDir
        val tracing = gradle.sharedServices.registerIfAbsent("tracing", TracingService::class.java) { spec ->
            spec.parameters.pid.set(pid())
            spec.parameters.project.set(gradle.startParameter.currentDir.name)
            spec.parameters.cmdline.set(gradle.startParameter.taskNames.joinToString(" "))
            spec.parameters.destination.set(projectDir.resolve("build"))
            spec.parameters.tasks.set(objectFactory.mapProperty(String::class.java, Long::class.java))
        }
        buildEventsListenerRegistry.onTaskCompletion(tracing)
        gradle.taskGraph.beforeTask { task ->
            val taskIdentity = (task as TaskInternal).taskIdentity
            tracing.get().parameters.tasks.put(taskIdentity.identityPath.toString(), tid())
        }
    }

}