package com.faire.detekt.rules

import dev.detekt.api.Finding
import dev.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val ISSUE_DESCRIPTION = "Do not call .values() on an Enum. Use .entries instead"

internal class UseEntriesInsteadOfValuesOnEnumTest {
  private lateinit var rule: UseEntriesInsteadOfValuesOnEnum

  @BeforeEach
  fun setup() {
    rule = UseEntriesInsteadOfValuesOnEnum()
  }

  @Test
  fun `calling entries on Enum class does not report`() {
    val findings = lint(
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
    val findings = lint(
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
    val findings = lint(
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

  private fun lint(content: String): List<Finding> = rule.lint(content)
}
