package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRuleTest
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val ISSUE_DESCRIPTION = "use firstNotNullOf() instead of mapNotNull followed by first()"

internal class UseFirstNotNullOfTest : AutoCorrectRuleTest<UseFirstNotNullOf>(
    { UseFirstNotNullOf(it) },
) {
  @Test
  fun `mapNotNull {} and first() consecutive call pair is corrected, and redundant asSequence is removed`() {
    assertLintAndFormat(
        """
           fun `test usage`() {
               products
                   .asSequence()
                   .mapNotNull { it.foo() }
                   .first()
           }
        """.trimIndent(),
        """
           fun `test usage`() {
               products
                   .firstNotNullOf { it.foo() }
           }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )

    assertLintAndFormat(
        """
           fun `test usage`() {
               numbers
                   .asSequence()
                   .mapNotNull(bar)
                   .first()
           }
        """.trimIndent(),
        """
           fun `test usage`() {
               numbers
                   .firstNotNullOf(bar)
           }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `necessary asSequence is not removed`() {
    assertLintAndFormat(
        """
           fun `test usage`() {
               products
                   .asSequence()
                   .map { product -> foo(product) }
                   .mapNotNull { it.bar() }
                   .first()
           }
        """.trimIndent(),
        """
           fun `test usage`() {
               products
                   .asSequence()
                   .map { product -> foo(product) }
                   .firstNotNullOf { it.bar() }
           }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `first with predicate function is not caught`() {
    assertThat(
        rule.lint(
            """
               fun `test usage`() {
                   products
                       .asSequence()
                       .map { product -> foo(product) }
                       .mapNotNull { it.bar() }
                       .first { it.quantity > 10 }
               }
               
               fun `test usage`() {
                   products
                       .asSequence()
                       .map { product -> bar(product) }
                       .first(xyz)
               }
            """.trimIndent(),
        ),
    ).isEmpty()
  }

  @Test
  fun `first() paired with other collection operations is not flagged`() {
    assertThat(
        rule.lint(
            """
               fun `test usage`() {
                   products.map { it.foo() }.first()
               }
            """.trimIndent(),
        ),
    ).isEmpty()
  }

  @Test
  fun `first() by itself is not flagged`() {
    assertThat(
        rule.lint(
            """
               fun `test usage`() {
                   val product = products.first()
               }
            """.trimIndent(),
        ),
    ).isEmpty()
  }
}
