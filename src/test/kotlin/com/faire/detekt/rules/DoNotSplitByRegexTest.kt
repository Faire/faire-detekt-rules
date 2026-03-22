package com.faire.detekt.rules

import dev.detekt.test.junit.KotlinCoreEnvironmentTest
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.KotlinEnvironmentContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class DoNotSplitByRegexTest(private val env: KotlinEnvironmentContainer) {
  private lateinit var rule: DoNotSplitByRegex

  @BeforeEach
  fun setUp() {
    rule = DoNotSplitByRegex()
  }

  @Test
  fun `split by regex is not allowed`() {
    val findings = rule.lintWithContext(
        env,
        """
        fun func(): List<String> {
          val str = "a,b,c"
          val sep = ",".toRegex()
          return str.split(sep)
        }
        """.trimIndent(),
    )
    assertThat(findings).hasSize(1)
  }

  @Test
  fun `split by regex is not allowed even when it is not the first arg`() {
    val findings = rule.lintWithContext(
        env,
        """
        fun func(): List<String> {
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
    val findings = rule.lintWithContext(
        env,
        """
        fun func(): List<String> {
          val str = "a,b,c"
          val sep = ","
          return str.split(sep)
        }
        """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }
}
