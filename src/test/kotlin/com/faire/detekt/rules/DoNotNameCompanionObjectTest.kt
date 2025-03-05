package com.faire.detekt.rules

import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DoNotNameCompanionObjectTest {
  private lateinit var rule: DoNotNameCompanionObject

  @BeforeEach
  fun setup() {
    rule = DoNotNameCompanionObject()
  }

  @Test
  fun `named companion object is flagged`() {
    val findings = rule.lint(
        """
          class Foo {
            companion object Bar
          }
        """.trimIndent(),
    )
    assertThat(findings).hasSize(1)
  }

  @Test
  fun `unnamed companion object is not flagged`() {
    val findings = rule.lint(
        """
          class Foo {
            companion object
          }
        """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }

  @Test
  fun `named non-companion object is not flagged`() {
    val findings = rule.lint(
        """
          class Foo {
            object Bar
          }
        """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }
}
