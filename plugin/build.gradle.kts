plugins {
    `java-gradle-plugin`
    kotlin("jvm")
    kotlin("kapt")
    id("io.johnsonlee.sonatype-publish-plugin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val agpVersion: String by extra
val boosterVersion: String by extra

dependencies {
    implementation(kotlin("bom"))
    implementation(kotlin("stdlib"))
    implementation("com.google.auto.service:auto-service:1.0")
    implementation("com.didiglobal.booster:booster-gradle-plugin:${boosterVersion}")
    implementation("com.didiglobal.booster:booster-api:${boosterVersion}")
    implementation("com.didiglobal.booster:booster-transform-asm:${boosterVersion}")

    kapt("com.google.auto.service:auto-service:1.0")

    compileOnly("com.android.tools.build:gradle:${agpVersion}")
}

gradlePlugin {
    plugins {
        create("${rootProject.name.capitalize()}Plugin") {
            id = "${rootProject.group}"
            implementationClass = "io.johnsonlee.template.gradle.TemplatePlugin"
        }
    }
}
