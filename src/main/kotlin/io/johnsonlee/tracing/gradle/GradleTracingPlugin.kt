package io.johnsonlee.tracing.gradle

import org.gradle.api.Plugin
import org.gradle.api.invocation.Gradle

class GradleTracingPlugin : Plugin<Gradle> {

    override fun apply(gradle: Gradle) {
        gradle.run(::TracingListener)
    }

}