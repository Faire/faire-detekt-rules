package com.faire.detekt.rules

import io.github.detekt.test.utils.KotlinCoreEnvironmentWrapper
import io.github.detekt.test.utils.createEnvironment
import io.gitlab.arturbosch.detekt.test.lintWithContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


internal class DoNotAccessVisibleForTestingTest {
  private lateinit var rule: DoNotAccessVisibleForTesting
  private lateinit var envWrapper: KotlinCoreEnvironmentWrapper

  @BeforeEach
  fun setup() {
    rule = DoNotAccessVisibleForTesting()
    envWrapper = createEnvironment(listOf())
  }

  @AfterEach
  fun tearDown() {
    envWrapper.dispose()
  }

  // We do not want to produce duplicate findings when the symbol is later used.
  @Test
  fun `test importing test-only symbol`() {
    val findings = rule.lintWithContext(
        envWrapper.env,
        """
          package bar

          import foo.test
        """,
        """
          package foo

          annotation class VisibleForTesting

          @VisibleForTesting
          fun test() {}
        """,
    )
    assertThat(findings).isEmpty()
  }

  @Test
  fun `test accessing test-only members`() {
    val findings = rule.lintWithContext(
        envWrapper.env,
        """
          package bar

          import foo.Foo

          fun bar(): Int {
            val foo = Foo()
            foo.testMember()
            foo.testProperty
          }
        """,
        """
          package foo

          annotation class VisibleForTesting

          class Foo {
            @VisibleForTesting
            val testProperty = 42

            @VisibleForTesting
            fun testMember() {}
          }
        """,
    )
    assertThat(findings).hasSize(2)
    assertThat(findings).allMatch { it.id == "DoNotAccessVisibleForTesting" }
  }
}
