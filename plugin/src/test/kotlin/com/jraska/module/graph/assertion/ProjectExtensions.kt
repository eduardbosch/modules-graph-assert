@file:JvmName("ProjectTestExtensions")

package com.jraska.module.graph.assertion

import org.gradle.api.Project

fun Project.collectAndRegisterDependencies() {
  val dependencyCollectorService = getDependencyCollectorService()!!
  (subprojects + this).forEach { project ->
    dependencyCollectorService.registerProjectDependencies(project.moduleDisplayName(), project.collectOwnDependencies())
  }
}
