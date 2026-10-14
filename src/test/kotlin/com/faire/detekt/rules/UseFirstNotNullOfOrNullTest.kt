package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRuleTest
import dev.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val ISSUE_DESCRIPTION = "use firstNotNullOfOrNull() instead of mapNotNull followed by firstOrNull()"

internal class UseFirstNotNullOfOrNullTest :
    AutoCorrectRuleTest<UseFirstNotNullOfOrNull>(
    { UseFirstNotNullOfOrNull(it) },
) {
  @Test
  fun `mapNotNull {} and firstOrNull() consecutive call pair is corrected, and redundant asSequence is removed`() {
    assertLintAndFormat(
        """
           fun `test usage`() {
               products
                   .asSequence()
                   .mapNotNull { it.foo() }
                   .firstOrNull()
           }
        """.trimIndent(),
        """
           fun `test usage`() {
               products
                   .firstNotNullOfOrNull { it.foo() }
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
                   .firstOrNull()
           }
        """.trimIndent(),
        """
           fun `test usage`() {
               numbers
                   .firstNotNullOfOrNull(bar)
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
                   .firstOrNull()
           }
        """.trimIndent(),
        """
           fun `test usage`() {
               products
                   .asSequence()
                   .map { product -> foo(product) }
                   .firstNotNullOfOrNull { it.bar() }
           }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `firstOrNull with predicate function is not caught`() {
    assertThat(
        rule.lint(
            """
               fun `test usage`() {
                   products
                       .asSequence()
                       .map { product -> foo(product) }
                       .mapNotNull { it.bar() }
                       .firstOrNull { it.quantity > 10 }
               }
               
               fun `test usage`() {
                   products
                       .asSequence()
                       .map { product -> bar(product) }
                       .firstOrNull(xyz)
               }
            """.trimIndent(),
        ),
    ).isEmpty()
  }

  @Test
  fun `firstOrNull() paired with other collection operations is not flagged`() {
    assertThat(
        rule.lint(
            """
               fun `test usage`() {
                   products.map { it.foo() }.firstOrNull()
               }
            """.trimIndent(),
        ),
    ).isEmpty()
  }

  @Test
  fun `unrelated earlier firstOrNull() is not corrupted`() {
    assertLintAndFormat(
        """
           fun `test usage`() {
               val a = otherList
                   .firstOrNull()
               val b = items
                   .mapNotNull { it.foo() }
                   .firstOrNull()
           }
        """.trimIndent(),
        """
           fun `test usage`() {
               val a = otherList
                   .firstOrNull()
               val b = items
                   .firstNotNullOfOrNull { it.foo() }
           }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `mapNotNull {} followed by first() is not flagged`() {
    assertThat(
        rule.lint(
            """
               fun `test usage`() {
                   products.mapNotNull { it.foo() }.first()
               }
            """.trimIndent(),
        ),
    ).isEmpty()
  }

  @Test
  fun `firstOrNull() by itself is not flagged`() {
    assertThat(
        rule.lint(
            """
               fun `test usage`() {
                   val product = products.firstOrNull()
               }
            """.trimIndent(),
        ),
    ).isEmpty()
  }
}
