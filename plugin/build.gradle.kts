import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    kotlin("jvm")
    id("io.johnsonlee.sonatype-publish-plugin")
}

dependencies {
    implementation(kotlin("bom"))
    implementation(kotlin("stdlib"))
    implementation("io.johnsonlee:once:1.2.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.0")
    implementation(project(":events"))
    testImplementation(kotlin("test-junit"))
    testImplementation("junit:junit:4.13.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

gradlePlugin {
    plugins {
        create("GradleTracingPlugin") {
            id = "${rootProject.group}"
            implementationClass = "io.johnsonlee.tracing.gradle.GradleTracingPlugin"
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xskip-metadata-version-check")
        jvmTarget = "1.8"
    }
}
