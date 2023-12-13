package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRuleTest
import io.gitlab.arturbosch.detekt.test.assertThat
import io.gitlab.arturbosch.detekt.test.lint
import org.junit.jupiter.api.Test

private const val ISSUE_DESCRIPTION = "Do not use isEqualTo(true) or isEqualTo(false), use isTrue() or isFalse()"

internal class AlwaysUseIsTrueOrIsFalseTest : AutoCorrectRuleTest<AlwaysUseIsTrueOrIsFalse> (
    { AlwaysUseIsTrueOrIsFalse(it) },
) {
  @Test
  fun `asserting isEqualTo for a boolean string is not caught or corrected`() {
    var findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo("true")
          }
        """.trimIndent(),
    )
    assertThat(findings).isEmpty()

    findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo("false")
          }
        """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }

  @Test
  fun `using isTrue is not corrected`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(someValue).isTrue()
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `using isFalse is not corrected`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(someValue).isFalse()
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `using isEqualTo(true) is corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo(true)
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(someValue).isTrue()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `using isEqualTo(false) is corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo(false)
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(someValue).isFalse()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }
}
