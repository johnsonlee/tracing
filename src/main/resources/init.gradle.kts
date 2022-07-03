import io.johnsonlee.tracing.gradle.GradleTracingPlugin

initscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath("io.johnsonlee.tracing:tracing:0.4.0")
    }
}

apply<GradleTracingPlugin>()