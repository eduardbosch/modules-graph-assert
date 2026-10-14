package com.jraska.module.graph.assertion

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.util.concurrent.ConcurrentHashMap

/**
 * Build service for collecting dependency information across all projects.
 * This is required for Gradle's isolated projects feature, where projects
 * cannot directly access configurations of other projects.
 *
 * Dependencies are collected for every configuration and filtered later, when the graph is built.
 * The configurations to analyze come from the `moduleGraphAssert` extension, which may be
 * evaluated after other projects have already registered their dependencies.
 */
abstract class DependencyCollectorService : BuildService<BuildServiceParameters.None> {

  private val projectDependencies = ConcurrentHashMap<String, Map<String, List<String>>>()
  private val projectAliases = ConcurrentHashMap<String, String>()

  fun registerProjectDependencies(projectName: String, dependenciesByConfiguration: Map<String, List<String>>) {
    projectDependencies[projectName] = dependenciesByConfiguration
  }

  fun registerProjectAlias(projectName: String, alias: String) {
    projectAliases[projectName] = alias
  }

  fun hasCollectedProjects(): Boolean = projectDependencies.isNotEmpty()

  fun getAllProjectDependencies(configurations: Set<String>): List<Pair<String, List<String>>> =
    projectDependencies.map { (projectName, dependenciesByConfiguration) ->
      projectName to dependenciesByConfiguration
        .filterKeys { configurations.contains(it) }
        .values
        .flatten()
    }

  fun getAllProjectAliases(): Map<String, String> =
    projectAliases.toMap()
}

/**
 * Collects dependencies from a single project (the one this is called on), grouped by configuration.
 * This avoids cross-project configuration access.
 */
fun Project.collectOwnDependencies(): Map<String, List<String>> =
  configurations
    .associate { configuration ->
      configuration.name to configuration.dependencies
        .filterIsInstance<ProjectDependency>()
        .map { project(it.path).moduleDisplayName() }
    }
    .filterValues { it.isNotEmpty() }
