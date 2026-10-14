package com.jraska.module.graph.assertion

import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class CustomConfigurationsGradleTest {
  @get:Rule
  val testProjectDir: TemporaryFolder = TemporaryFolder()

  @Before
  fun setup() {
    testProjectDir.newFile("settings.gradle").writeText(
      """
      plugins {
          id 'io.github.eduardbosch.module.graph.assertion.settings'
      }
      include ':app', ':core', ':core-testing'
    """
    )

    createModule("core", content = "apply plugin: 'java-library'")

    createModule(
      "core-testing", content = """
      apply plugin: 'java-library'

      dependencies {
        implementation project(":core")
      }
      """
    )

    createModule(
      "app", content = """
          plugins {
              id 'io.github.eduardbosch.module.graph.assertion'
          }
          apply plugin: 'java-library'

          moduleGraphAssert {
            configurations = ['implementation', 'testImplementation']
            restricted = [':app -X> :core-testing']
          }

          dependencies {
            implementation project(":core")
            testImplementation project(":core-testing")
          }
      """
    )
  }

  @Test
  fun includesDependenciesFromConfiguredConfigurations() {
    val output = runGradleAssertModuleGraph(testProjectDir.root, "generateModulesGraphStatistics").output

    assert(output.contains("GraphStatistics(modulesCount=3, edgesCount=3, height=2, longestPath=':app -> :core-testing -> :core')"))
  }

  @Test
  fun failsOnRestrictedDependencyFromConfiguredConfiguration() {
    val output = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withPluginClasspath()
      .withArguments("--configuration-cache", "assertModuleGraph", "-Dorg.gradle.unsafe.isolated-projects=true")
      .buildAndFail()
      .output

    assert(output.contains(":app -X> :core-testing"))
  }

  private fun createModule(dir: String, content: String) {
    val newFolder = testProjectDir.newFolder(dir)
    File(newFolder, "build.gradle").writeText(content)
  }
}
