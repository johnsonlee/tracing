package io.johnsonlee.template.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class TemplatePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        println("Applying ${javaClass.name} ...")
    }

}