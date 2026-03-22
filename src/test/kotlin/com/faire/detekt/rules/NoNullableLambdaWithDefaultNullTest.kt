package com.faire.detekt.rules

import dev.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class NoNullableLambdaWithDefaultNullTest {
  private val rule: NoNullableLambdaWithDefaultNull = NoNullableLambdaWithDefaultNull()

  @Test
  fun `should report nullable callback with default null`() {
    val findings = rule.lint(
        """
            fun test(callback: ((Int) -> Unit)? = null) {}
        """.trimIndent(),
    )

    assertThat(findings).hasSize(1)
    assertThat(findings.first().message).isEqualTo(
        "Replace 'null' with an empty lambda expression '{}' for the default value of the function parameter.",
    )
  }

  @Test
  fun `should not report non-nullable callback with empty lambda`() {
    val findings = rule.lint(
        """
            fun test1(callback: (Int) -> Unit = {}) {}
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }
}
