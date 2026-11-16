package com.jraska.module.graph.assertion

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CollectOwnDependenciesTest {

  private lateinit var rootProject: Project
  private lateinit var appProject: Project
  private lateinit var coreProject: Project

  @Before
  fun setUp() {
    rootProject = ProjectBuilder.builder().withName("root").build()
    appProject = createProject("app")
    coreProject = createProject("core")
  }

  @Test
  fun doesNotRealizePendingDependenciesOfResolvableConfigurations() {
    val lateDependencies = mutableListOf<Dependency>()
    val classpath = appProject.configurations.getByName("testCompileClasspath")
    classpath.dependencies.addAllLater(appProject.provider { lateDependencies.toList() })

    appProject.collectOwnDependencies()
    lateDependencies.add(appProject.dependencies.create(coreProject))

    val classpathDependencies = classpath.dependencies.filterIsInstance<ProjectDependency>().map { it.path }
    assertEquals(listOf(":core"), classpathDependencies)
  }

  @Test
  fun collectsDeclaredDependenciesOfResolvableConfigurations() {
    appProject.dependencies.add("annotationProcessor", coreProject)

    val dependencies = appProject.collectOwnDependencies()

    assertEquals(listOf(":core"), dependencies["annotationProcessor"])
  }

  @Test
  fun collectsPendingDependenciesOfDeclarableConfigurations() {
    appProject.configurations.getByName("implementation").dependencies
      .addLater(appProject.provider { appProject.dependencies.create(coreProject) })

    val dependencies = appProject.collectOwnDependencies()

    assertEquals(listOf(":core"), dependencies["implementation"])
  }

  private fun createProject(name: String): Project {
    val project = ProjectBuilder.builder()
      .withName(name)
      .withParent(rootProject)
      .build()

    project.plugins.apply(JavaLibraryPlugin::class.java)
    return project
  }
}
