package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRuleTest
import dev.detekt.api.modifiedText
import dev.detekt.test.TestConfig
import dev.detekt.test.lint
import dev.detekt.test.utils.compileContentForTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val ISSUE_DESCRIPTION = "Do not call containsExactly() with no arguments, call isEmpty()."

internal class DoNotUseContainsExactlyForEmptyListInAssertTest :
    AutoCorrectRuleTest<DoNotUseContainsExactlyForEmptyListInAssert>(
    { DoNotUseContainsExactlyForEmptyListInAssert(it) },
) {

  @Test
  fun `containsExactly() is caught and corrected in assertion`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(someCollection).containsExactly()
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(someCollection).isEmpty()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `containsExactly() on its own line is corrected in place`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(someCollection)
                .containsExactly()
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(someCollection)
                .isEmpty()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `containsExactly with arguments is okay`() {
    assertNoViolations(
        """
          fun `test usage`() {
            assertThat(someCollection).containsExactly(1, 2)
            assertThat(someCollection).containsExactly(*expected)
          }
        """.trimIndent(),
    )
  }

  @Test
  fun `containsExactly() outside an assertion is okay`() {
    assertNoViolations(
        """
          fun `test usage`() {
            if (someCollection.containsExactly()) return
          }
        """.trimIndent(),
    )
  }

  @Test
  fun `containsExactly() followed by another call is caught but not corrected`() {
    assertLintedButNotFormatted(
        """
          fun `test usage`() {
            assertThat(someCollection).containsExactly().doesNotContainNull()
          }
        """.trimIndent(),
    )
  }

  @Test
  fun `only the containsExactly() that ends an assertion is corrected`() {
    val code = compileContentForTest(
        """
          fun `test usage`() {
            assertThat(firstCollection).containsExactly().doesNotContainNull()
            assertThat(secondCollection).containsExactly()
          }
        """.trimIndent(),
    )

    val findings = DoNotUseContainsExactlyForEmptyListInAssert(TestConfig("autoCorrect" to true)).lint(code)

    assertThat(findings).hasSize(2)
    assertThat(code.modifiedText).isEqualTo(
        """
          fun `test usage`() {
            assertThat(firstCollection).containsExactly().doesNotContainNull()
            assertThat(secondCollection).isEmpty()
          }
        """.trimIndent(),
    )
  }
}
