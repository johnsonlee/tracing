package io.johnsonlee.tracing.gradle

import io.johnsonlee.tracing.event.pid
import org.gradle.api.Plugin
import org.gradle.api.invocation.Gradle
import org.gradle.build.event.BuildEventsListenerRegistry
import javax.inject.Inject

@Suppress("UnstableApiUsage")
class GradleTracingPlugin @Inject constructor(
        private val buildEventsListenerRegistry: BuildEventsListenerRegistry
) : Plugin<Gradle> {

    override fun apply(gradle: Gradle) {
        val ts = System.currentTimeMillis()
        val projectDir = gradle.startParameter.projectDir ?: gradle.startParameter.currentDir
        val tracing = gradle.sharedServices.registerIfAbsent("tracing", TracingService::class.java) { spec ->
            spec.parameters.apply {
                pid.set(pid())
                project.set(gradle.startParameter.currentDir.name)
                cmdline.set(gradle.startParameter.taskNames.joinToString(" "))
                destination.set(projectDir.resolve("build"))
            }
        }
        buildEventsListenerRegistry.onTaskCompletion(tracing)
    }

}