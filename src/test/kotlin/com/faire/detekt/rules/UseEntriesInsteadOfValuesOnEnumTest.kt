package com.faire.detekt.rules

import io.github.detekt.test.utils.KotlinCoreEnvironmentWrapper
import io.github.detekt.test.utils.createEnvironment
import io.gitlab.arturbosch.detekt.api.Finding
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val ISSUE_DESCRIPTION = "Do not call .values() on an Enum. Use .entries instead"

internal class UseEntriesInsteadOfValuesOnEnumTest {
  private lateinit var rule: UseEntriesInsteadOfValuesOnEnum
  private lateinit var envWrapper: KotlinCoreEnvironmentWrapper

  @BeforeEach
  fun setup() {
    rule = UseEntriesInsteadOfValuesOnEnum()
    envWrapper = createEnvironment(listOf())
  }

  @AfterEach
  fun tearDown() {
    envWrapper.dispose()
  }

  @Test
  fun `calling entries on Enum class does not report`() {
    val findings = lintWithTypeResolution(
        """
          enum class EnumBar {
            FOO, BAR
          }
          
          fun tester(): EnumBar {
            return EnumBar.entries.first()
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `calling values on a map does not report`() {
    val findings = lintWithTypeResolution(
        """
          fun tester(): String {
            val map = mapOf("foo" to "bar")
            
            return map.values.first()
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `calling values() on Enum class does report`() {
    val findings = lintWithTypeResolution(
        """
          enum class EnumBar {
            FOO, BAR
          }
          
          fun tester(): EnumBar {
            return EnumBar.values().first()
          }
        """.trimIndent(),
    )

    assertThat(findings.map { it.message }).containsExactlyInAnyOrder(ISSUE_DESCRIPTION)
  }

  private fun lintWithTypeResolution(content: String): List<Finding> {
    return rule.compileAndLintWithContext(envWrapper.env, content)
  }
}
