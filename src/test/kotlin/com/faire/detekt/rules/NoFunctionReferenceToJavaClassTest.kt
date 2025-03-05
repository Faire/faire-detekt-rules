package com.faire.detekt.rules

import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class NoFunctionReferenceToJavaClassTest {
  private val rule: NoFunctionReferenceToJavaClass = NoFunctionReferenceToJavaClass()

  @Test
  fun `should report double-colon javaClass`() {
    val findings = rule.lint(
        """
        class Foo
        val buggy = Foo::javaClass.name
        """.trimIndent(),
    )

    assertThat(findings).hasSize(1)
    assertThat(findings.first().issue.id).isEqualTo("NoFunctionReferenceToJavaClass")
  }

  @Test
  fun `should not report dot javaClass`() {
    val findings = rule.lint(
        """
          class Foo
          val correct = Foo.javaClass.name
          """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }
}
