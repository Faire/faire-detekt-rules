package com.faire.detekt.rules

import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val ISSUE_DESCRIPTION = "Do not use isZero(), use isEqualTo(0) instead."

internal class DoNotUseIsZeroAssertionsTest {

  private lateinit var rule: DoNotUseIsZeroAssertions

  @BeforeEach
  fun setup() {
    rule = DoNotUseIsZeroAssertions()
  }
  @Test
  fun `using isZero causes failure`() {
    val findings = rule.lint(
        """
           fun `test usage`() {
               assertThat(products).isZero()
           }
        """.trimIndent(),
    )

    assertThat(findings.first().message).isEqualTo(ISSUE_DESCRIPTION)
  }

  @Test
  fun `using isZero outside of an assertion does not cause failure`() {
    val findings = rule.lint(
        """
           fun String.isZero(): Boolean = false
           fun `test usage`() {
               "test".isZero()
           }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }
}
