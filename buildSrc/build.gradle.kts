plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
    kotlin("kapt") version embeddedKotlinVersion
}

repositories {
    mavenCentral()
    google()
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

sourceSets {
    main {
        java {
            srcDirs("../plugin/src/main/java")
        }
    }
}

gradlePlugin {
    plugins {
        create("TemplatePlugin") {
            id = "io.johnsonlee.template-gradle-plugin"
            implementationClass = "io.johnsonlee.template.gradle.TemplatePlugin"
        }
    }
}
