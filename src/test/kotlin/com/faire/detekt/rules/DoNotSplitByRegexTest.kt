package com.faire.detekt.rules

import io.github.detekt.test.utils.KotlinCoreEnvironmentWrapper
import io.github.detekt.test.utils.createEnvironment
import io.gitlab.arturbosch.detekt.test.lintWithContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DoNotSplitByRegexTest {
  private lateinit var rule: DoNotSplitByRegex
  private lateinit var envWrapper: KotlinCoreEnvironmentWrapper

  @BeforeEach
  fun setUp() {
    rule = DoNotSplitByRegex()
    envWrapper = createEnvironment(listOf())
  }

  @AfterEach
  fun tearDown() {
    envWrapper.dispose()
  }

  @Test
  fun `split by regex is not allowed`() {
    val findings = rule.lintWithContext(
        envWrapper.env,
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
  fun `split by regex is not allowed even when it is not the first arg`() {
    val findings = rule.lintWithContext(
        envWrapper.env,
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
    val findings = rule.lintWithContext(
        envWrapper.env,
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
