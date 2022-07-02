import org.gradle.api.Project.DEFAULT_VERSION
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }

    val agpVersion: String by extra

    dependencies {
        classpath("com.android.tools.build:gradle:${agpVersion}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${embeddedKotlinVersion}")
        classpath("io.johnsonlee:sonatype-publish-plugin:1.6.1")
    }
}

group = "io.johnsonlee"
version = project.findProperty("version")?.takeIf { it != DEFAULT_VERSION } ?: "1.0.0-SNAPSHOT"

allprojects {

    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
        google()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xskip-metadata-version-check")
            jvmTarget = "1.8"
        }
    }
}