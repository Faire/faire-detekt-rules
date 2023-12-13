package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRuleTest
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val ISSUE_DESCRIPTION = "Do not call hasSize(0) on an empty collection, call isEmpty()."

internal class DoNotUseHasSizeForEmptyListInAssertTest : AutoCorrectRuleTest<DoNotUseHasSizeForEmptyListInAssert>(
    { DoNotUseHasSizeForEmptyListInAssert(it) },
) {

  @Test
  fun `hasSize(0) is caught and corrected in assertion`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(someCollection).hasSize(0)
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(someCollection).isEmpty()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `hasSize(2) is okay`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(someCollection).hasSize(2)
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `hasSize(0) in if statement is okay`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            if(someCollection.hasSize(0)) return
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }
}
