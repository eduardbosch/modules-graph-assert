package com.jraska.module.graph.assertion

import com.jraska.module.graph.assertion.ModuleGraphAssertionsSettingsPlugin.Companion.SERVICE_NAME
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.provider.Provider

fun Project.registerDependencyCollectorService(): Provider<DependencyCollectorService> =
  gradle.registerDependencyCollectorService()

fun Settings.registerDependencyCollectorService(): Provider<DependencyCollectorService> =
  gradle.registerDependencyCollectorService()

private fun Gradle.registerDependencyCollectorService(): Provider<DependencyCollectorService> =
  sharedServices
    .registerIfAbsent(SERVICE_NAME, DependencyCollectorService::class.java) {}

fun Project.getDependencyCollectorService(): DependencyCollectorService? =
  gradle
    .sharedServices
    .registrations
    .findByName(SERVICE_NAME)
    ?.service
    ?.get() as? DependencyCollectorService
