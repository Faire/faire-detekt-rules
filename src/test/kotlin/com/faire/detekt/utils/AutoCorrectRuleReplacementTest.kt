package com.faire.detekt.utils

import com.faire.detekt.rules.AlwaysUseIsTrueOrIsFalse
import com.faire.detekt.rules.DoNotAssertIsEqualOnTheResultOfSingle
import com.faire.detekt.rules.DoNotUsePropertyAccessInAssert
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

    ReplaceCalls("foo", "bar()").lint(ktFile)

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
  fun `applies only the innermost of overlapping replacements`() {
    val ktFile = compileContentForTest("val x = foo(foo())")

    ReplaceCalls("foo", "bar()").lint(ktFile)

    assertThat(ktFile.modifiedText).isEqualTo("val x = foo(bar())")
  }

  @Test
  fun `leaves the file untouched when nothing is replaced`() {
    val ktFile = compileContentForTest("val x = baz()")

    ReplaceCalls("foo", "bar()").lint(ktFile)

    assertThat(ktFile.modifiedText).isNull()
  }

  @Test
  fun `applies a replacement after another rule added matching text before it`() {
    val ktFile = compileContentForTest(
        """
          fun test() {
            baz()
            foo()
          }
        """.trimIndent(),
    )

    ReplaceCalls("baz", "foo()").lint(ktFile)
    ReplaceCalls("foo", "bar()").lint(ktFile)

    assertThat(ktFile.modifiedText).isEqualTo(
        """
          fun test() {
            foo()
            bar()
          }
        """.trimIndent(),
    )
  }

  @Test
  fun `skips a replacement whose element another rule already replaced`() {
    val ktFile = compileContentForTest(
        """
          fun test() {
            foo()
            val s = "foo()"
          }
        """.trimIndent(),
    )

    ReplaceCalls("foo", "baz()").lint(ktFile)
    ReplaceCalls("foo", "bar()").lint(ktFile)

    assertThat(ktFile.modifiedText).isEqualTo(
        """
          fun test() {
            baz()
            val s = "foo()"
          }
        """.trimIndent(),
    )
  }

  @Test
  fun `adds parentheses to a property access below an assertion another rule rewrote`() {
    val ktFile = compileContentForTest(
        """
          fun test() {
            assertThat(a).isEqualTo(true)
            assertThat(b).isTrue
          }
        """.trimIndent(),
    )

    AlwaysUseIsTrueOrIsFalse(autoCorrectConfig).lint(ktFile)
    DoNotUsePropertyAccessInAssert(autoCorrectConfig).lint(ktFile)

    assertThat(ktFile.modifiedText).isEqualTo(
        """
          fun test() {
            assertThat(a).isTrue()
            assertThat(b).isTrue()
          }
        """.trimIndent(),
    )
  }

  @Test
  fun `leaves a string literal alone after another rule rewrote the code it repeats`() {
    val ktFile = compileContentForTest(
        """
          fun test() {
            assertThat(xs.single()).isEqualTo(true)
            val s = "assertThat(xs.single()).isEqualTo(true)"
          }
        """.trimIndent(),
    )

    AlwaysUseIsTrueOrIsFalse(autoCorrectConfig).lint(ktFile)
    DoNotAssertIsEqualOnTheResultOfSingle(autoCorrectConfig).lint(ktFile)

    assertThat(ktFile.modifiedText).isEqualTo(
        """
          fun test() {
            assertThat(xs.single()).isTrue()
            val s = "assertThat(xs.single()).isEqualTo(true)"
          }
        """.trimIndent(),
    )
  }

  @Test
  fun `applies replacements on top of lines another tool added`() {
    val ktFile = compileContentForTest(
        """
          fun test() {
            foo()
            foo()
          }
        """.trimIndent(),
    )
    ktFile.modifiedText = "// Added by another tool\n${ktFile.text}"

    ReplaceCalls("foo", "bar()").lint(ktFile)

    assertThat(ktFile.modifiedText).isEqualTo(
        """
          // Added by another tool
          fun test() {
            bar()
            bar()
          }
        """.trimIndent(),
    )
  }

  @Test
  fun `skips a replacement on a line another tool changed`() {
    val ktFile = compileContentForTest(
        """
          fun test() {
            foo( )
            foo()
          }
        """.trimIndent(),
    )
    ktFile.modifiedText = ktFile.text.replace("foo( )", "foo()")

    ReplaceCalls("foo", "bar()").lint(ktFile)

    assertThat(ktFile.modifiedText).isEqualTo(
        """
          fun test() {
            foo()
            bar()
          }
        """.trimIndent(),
    )
  }

  @Test
  fun `starts over once detekt has written the file`() {
    val ktFile = compileContentForTest(
        """
          fun test() {
            foo()
            baz()
          }
        """.trimIndent(),
    )
    ReplaceCalls("foo", "bar()").lint(ktFile)
    // detekt clears modifiedText after writing it, because the same PSI file may be analyzed again
    ktFile.modifiedText = null

    ReplaceCalls("baz", "qux()").lint(ktFile)

    assertThat(ktFile.modifiedText).isEqualTo(
        """
          fun test() {
            foo()
            qux()
          }
        """.trimIndent(),
    )
  }

  private class ReplaceCalls(private val name: String, private val replacement: String) :
      AutoCorrectRule(autoCorrectConfig, "Replace $name() with $replacement") {
    override fun visitCallExpression(expression: KtCallExpression) {
      super.visitCallExpression(expression)
      if (expression.calleeExpression?.text != name) return

      report(Finding(Entity.from(expression), description))
      if (autoCorrect) replace(expression, replacement)
    }
  }

  private companion object {
    private val autoCorrectConfig = TestConfig("autoCorrect" to true)
  }
}
