package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRuleTest
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private val ISSUE_DESCRIPTION = "Do not use property access syntax with assertion methods. " +
    "Do not remove the parenthesis."

internal class DoNotUsePropertyAccessInAssertTest : AutoCorrectRuleTest<DoNotUsePropertyAccessInAssert>(
    { DoNotUsePropertyAccessInAssert(it) },
) {

  @Test
  fun `check if property access is caught and corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            with(thirdBrandOrder) {
              assertThat(freeShipping).isFalse
              assertThat(freeShippingReason).isNull()
            }
            assertThat(something).isNotNull
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            with(thirdBrandOrder) {
              assertThat(freeShipping).isFalse()
              assertThat(freeShippingReason).isNull()
            }
            assertThat(something).isNotNull()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `allow lambda parameter`() {
    val findings = rule.lint(
        """
           fun test1() {
            assertThat(something.testField.single()).satisfies { a ->
              assertThat(a.name).isEqualTo("csv")
              assertThat(a.isInternal).isTrue()
            }
        }
        """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }

  @Test
  fun `allow good usages`() {
    val findings = rule.lint(
        """
          class Good {
            fun test1(a: String) {
              assertThat(foo).isTrue()
            }
          }          
        """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }
}
