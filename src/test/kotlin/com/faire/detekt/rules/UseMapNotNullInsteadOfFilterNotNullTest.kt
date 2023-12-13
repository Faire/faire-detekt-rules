package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRuleTest
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val ISSUE_DESCRIPTION = "use mapNotNull() instead of map followed by filerNotNull()"

internal class UseMapNotNullInsteadOfFilterNotNullTest : AutoCorrectRuleTest<UseMapNotNullInsteadOfFilterNotNull>(
    { UseMapNotNullInsteadOfFilterNotNull(it) },
) {

  @Test
  fun `map {} and filterNotNull() consecutive call pair is caught and corrected`() {
    assertLintAndFormat(
        """
           fun foo() {
               products.map { it.toPlaceOrderItem(cases) }
                       .filterNotNull()
           }
        """.trimIndent(),
        """
          fun foo() {
              products.mapNotNull { it.toPlaceOrderItem(cases) }
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )

    assertLintAndFormat(
        """
           fun foo() {
               numbers.map(::bar)
                      .filterNotNull()
           }
        """.trimIndent(),
        """
          fun foo() {
              numbers.mapNotNull(::bar)
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `filterNotNull() on unrelated type is not flagged`() {
    assertThat(
        rule.lint(
            """
               fun foo() {
                products.map { it.toPlaceOrderItem(cases) }.toList().filterNotNull()
              }
            """.trimIndent(),
        ),
    ).isEmpty()
  }

  @Test
  fun `filterNotNull() is not flagged`() {
    assertThat(
        rule.lint(
            """
              fun foo() {
                val testList = testList.filterNotNull()
              }
            """.trimIndent(),
        ),
    ).isEmpty()
  }
}
