package com.faire.detekt.utils

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.modifiedText
import dev.detekt.test.TestConfig
import dev.detekt.test.lint
import dev.detekt.test.utils.compileContentForTest
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.kotlin.psi.KtCallExpression
import org.junit.jupiter.api.Test

internal class AutoCorrectRuleReplacementTest {
  private val rule = ReplaceFooCallsWithBar(TestConfig("autoCorrect" to true))

  @Test
  fun `replaces the reported element rather than the first matching text`() {
    val ktFile = compileContentForTest(
        """
          // foo()
          val foo = "foo()"
          fun test() {
            foo()
          }
        """.trimIndent(),
    )

    rule.lint(ktFile)

    assertThat(ktFile.modifiedText).isEqualTo(
        """
          // foo()
          val foo = "foo()"
          fun test() {
            bar()
          }
        """.trimIndent(),
    )
  }

  @Test
  fun `applies replacements on top of changes made by other rules`() {
    val ktFile = compileContentForTest(
        """
          fun test() {
            foo()
            foo()
          }
        """.trimIndent(),
    )
    ktFile.modifiedText = "// Added by another rule\n${ktFile.text}"

    rule.lint(ktFile)

    assertThat(ktFile.modifiedText).isEqualTo(
        """
          // Added by another rule
          fun test() {
            bar()
            bar()
          }
        """.trimIndent(),
    )
  }

  @Test
  fun `applies only the innermost of overlapping replacements`() {
    val ktFile = compileContentForTest("val x = foo(foo())")

    rule.lint(ktFile)

    assertThat(ktFile.modifiedText).isEqualTo("val x = foo(bar())")
  }

  @Test
  fun `leaves the file untouched when nothing is replaced`() {
    val ktFile = compileContentForTest("val x = baz()")

    rule.lint(ktFile)

    assertThat(ktFile.modifiedText).isNull()
  }

  private class ReplaceFooCallsWithBar(config: Config) : AutoCorrectRule(config, "Replace foo() with bar()") {
    override fun visitCallExpression(expression: KtCallExpression) {
      super.visitCallExpression(expression)
      if (expression.calleeExpression?.text != "foo") return

      report(Finding(Entity.from(expression), description))
      if (autoCorrect) replace(expression, "bar()")
    }
  }
}
