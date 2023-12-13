package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRuleTest
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val ISSUE_DESCRIPTION = "Do not use isEqualTo(1), use isOne() instead."

internal class DoNotUseIsEqualToWhenArgumentIsOneTest : AutoCorrectRuleTest<DoNotUseIsEqualToWhenArgumentIsOne>(
    { DoNotUseIsEqualToWhenArgumentIsOne(it) },
) {

  @Test
  fun `checking if collection has 0 elements is not caught`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(someCollection.size).isEqualTo(1)
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `checking if value is one returned from function is caught and corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(getNumberOfOrders()).isEqualTo(1)
          }
          
          fun getNumberOfOrders(): Int = 1
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(getNumberOfOrders()).isOne()
          }
          
          fun getNumberOfOrders(): Int = 1
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `checking if value is one Int is caught and corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo(1)
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(someValue).isOne()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
    assertThat(1).isOne()
  }

  @Test
  fun `checking if value is one Long is caught and corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo(1L)
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(someValue).isOne()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
    assertThat(1L).isOne()
  }

  @Test
  fun `checking if value is 1 in if statement is okay`() {
    val findings = rule.lint(
        """
          fun usage() {
            if(someValue.isEqualTo(1L)) {
              doSomething()
            }
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `checking if value is not one is okay`() {
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
  fun `checking if value is not one negative is okay`() {
    var findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo(-5L)
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()

    findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo(-1L)
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `checking if value is equal to a string that contains a 1`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo("1x")
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `checking if expression is one is caught and corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(someValue1 + someValue2).isEqualTo(1L)
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(someValue1 + someValue2).isOne()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `checking if floating 1 is caught and corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo(1.0f)
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(someValue).isOne()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )

    assertThat(1.0f).isOne()
    assertThat(1f).isOne()
    assertThat(1.0F).isOne()
  }

  @Test
  fun `checking if 1 double is caught and corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo(1.0)
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(someValue).isOne()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )

    assertThat(1.0).isOne()
  }

  @Test
  fun `checking if 1 hex is caught and corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo(0x1)
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(someValue).isOne()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )

    assertThat(0x1).isOne()
  }

  @Test
  fun `checking if 1 binary is caught and corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(someValue).isEqualTo(0b1)
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(someValue).isOne()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )

    assertThat(0b1).isOne()
  }
}
