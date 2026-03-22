package com.faire.detekt.rules

import dev.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class NoExtensionFunctionOnNullableReceiverTest {
  private lateinit var rule: NoExtensionFunctionOnNullableReceiver

  @BeforeEach
  fun setup() {
    rule = NoExtensionFunctionOnNullableReceiver()
  }

  @Test
  fun `flag non-private extension function on nullable type`() {
    val findings = rule.lint(
        """
          fun String?.foo(): String? = this
        """.trimIndent(),
    )
    assertThat(findings).hasSize(1)
  }

  @Test
  fun `do not flag extension function on non-nullable type`() {
    val findings = rule.lint(
        """
          fun String.foo(): String? = this
        """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }

  @Test
  fun `do not flag extension function that returns non-null type`() {
    val findings = rule.lint(
        """
          fun String?.foo(): String = this ?: ""
        """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }

  @Test
  fun `do not flag if manually suppressed`() {
    val findings = rule.lint(
        """
          @Suppress("NoExtensionFunctionOnNullableReceiver")
          fun String?.foo(): String? = this
        """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }
}
