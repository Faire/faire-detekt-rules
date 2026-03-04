package com.faire.detekt.rules

import dev.detekt.test.utils.KotlinEnvironmentContainer
import dev.detekt.test.utils.createEnvironment
import dev.detekt.test.lintWithContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class DoNotAccessVisibleForTestingTest {
  private lateinit var rule: DoNotAccessVisibleForTesting
  private lateinit var envWrapper: KotlinEnvironmentContainer

  @BeforeEach
  fun setup() {
    rule = DoNotAccessVisibleForTesting()
    envWrapper = createEnvironment()
  }

  // We do not want to produce duplicate findings when the symbol is later used.
  @Test
  @Disabled("TODO: rule type-resolution logic needs Analysis API migration")
  fun `test importing test-only symbol`() {
    val findings = rule.lintWithContext(
        envWrapper,
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
  @Disabled("TODO: rule type-resolution logic needs Analysis API migration")
  fun `test accessing test-only members`() {
    val findings = rule.lintWithContext(
        envWrapper,
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
  }
}
