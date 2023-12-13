package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRuleTest
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val ISSUE_DESCRIPTION = "Do not use isEqualTo(0), use isZero() instead."

internal class DoNotUseIsEqualToWhenArgumentIsZeroTest : AutoCorrectRuleTest<DoNotUseIsEqualToWhenArgumentIsZero>(
    { DoNotUseIsEqualToWhenArgumentIsZero(it) },
) {
  @Test
  fun `checking if collection has 0 elements is not caught`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(someCollection.size).isEqualTo(0)
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `checking if value is zero Int is caught and corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo(0)
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(someValue).isZero()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `checking if value is zero Long is caught and corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo(0L)
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(someValue).isZero()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `checking if value is 0 in if statement is okay`() {
    val findings = rule.lint(
        """
          fun usage() {
            if(someValue.isEqualTo(0L)) {
              doSomething()
            }
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `checking if value is non-zero positive is okay`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo(10L)
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `checking if value is non-zero negative is okay`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo(-5L)
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `checking if value is equal to a string that contains a 0`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo("0x")
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `checking if expression is zero is caught and corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(someValue1 + someValue2).isEqualTo(0L)
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(someValue1 + someValue2).isZero()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `checking if floating 0 is caught and corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo(0.0f)
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(someValue).isZero()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )

    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo(0f)
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(someValue).isZero()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )

    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo(0.0F)
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(someValue).isZero()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `checking if 0 double is caught and corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo(0.0)
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(someValue).isZero()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `checking if 0 hex is caught and corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo(0x0)
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(someValue).isZero()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `checking if 0 binary is caught and corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo(0b0)
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(someValue).isZero()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }
}
