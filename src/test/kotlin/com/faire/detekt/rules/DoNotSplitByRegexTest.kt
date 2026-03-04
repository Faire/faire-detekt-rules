package com.faire.detekt.rules

import dev.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class DoNotSplitByRegexTest {
  private lateinit var rule: DoNotSplitByRegex

  @BeforeEach
  fun setUp() {
    rule = DoNotSplitByRegex()
  }

  @Test
  @Disabled("TODO: rule type-resolution logic needs Analysis API migration")
  fun `split by regex is not allowed`() {
    val findings = rule.lint(
        """
        fun func(): String {
          val str = "a,b,c"
          val sep = ",".toRegex()
          return str.split(sep)
        }
        """.trimIndent(),
    )
    assertThat(findings).hasSize(1)
  }

  @Test
  @Disabled("TODO: rule type-resolution logic needs Analysis API migration")
  fun `split by regex is not allowed even when it is not the first arg`() {
    val findings = rule.lint(
        """
        fun func(): String {
          val str = "a,b,c"
          val sep = ",".toRegex()
          return str.split(limit=2, regex=sep)
        }
        """.trimIndent(),
    )
    assertThat(findings).hasSize(1)
  }

  @Test
  fun `split by string literal is fine`() {
    val findings = rule.lint(
        """
        fun func(): String {
          val str = "a,b,c"
          val sep = ","
          return str.split(sep)
        }
        """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }
}
