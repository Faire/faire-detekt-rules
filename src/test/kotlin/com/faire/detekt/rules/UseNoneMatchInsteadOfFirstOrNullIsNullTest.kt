package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRuleTest
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val ISSUE_DESCRIPTION =
    "Use assertThat(collection).noneMatch { predicate } instead of " +
        "assertThat(collection.firstOrNull { predicate }).isNull()"

internal class UseNoneMatchInsteadOfFirstOrNullIsNullTest :
    AutoCorrectRuleTest<UseNoneMatchInsteadOfFirstOrNullIsNull>({
      UseNoneMatchInsteadOfFirstOrNullIsNull(it)
    }) {

  @Test
  fun `firstOrNull with lambda followed by isNull is caught and corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(items.firstOrNull { it.type == DELETED }).isNull()
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(items).noneMatch { it.type == DELETED }
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `firstOrNull with function reference followed by isNull is caught and corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            val predicate = { item: Item -> item.isDeleted }
            assertThat(items.firstOrNull(predicate)).isNull()
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            val predicate = { item: Item -> item.isDeleted }
            assertThat(items).noneMatch(predicate)
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `chained calls before firstOrNull is caught and corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(order.items.filter { it.active }.firstOrNull { it.type == TAX }).isNull()
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(order.items.filter { it.active }).noneMatch { it.type == TAX }
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `nested assertThat with firstOrNull isNull is caught and corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            with(response) {
              assertThat(discounts.firstOrNull { it.brandToken == brand.token }).isNull()
            }
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            with(response) {
              assertThat(discounts).noneMatch { it.brandToken == brand.token }
            }
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `firstOrNull without isNull is not caught`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(items.firstOrNull { it.type == DELETED }).isNotNull()
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `isNull without firstOrNull is not caught`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(item).isNull()
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `firstOrNull without predicate is not caught`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(items.firstOrNull()).isNull()
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `noneMatch is not caught`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(items).noneMatch { it.type == DELETED }
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `firstOrNull isNull not within assertThat is not caught`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            val result = items.firstOrNull { it.type == DELETED }
            if (result.isNull()) {
              // do something
            }
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `find followed by isNull is not caught`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(items.find { it.type == DELETED }).isNull()
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `multiline assertThat with firstOrNull preserves formatting`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            assertThat(
                retailerUser.getProductPage(productToken = jeansProduct.token)
                    .layoutElements.firstOrNull { it.onClickEventName == "web_pdp_shop_by_attributes" },
            ).isNull()
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            assertThat(
                retailerUser.getProductPage(productToken = jeansProduct.token)
                    .layoutElements,
            ).noneMatch { it.onClickEventName == "web_pdp_shop_by_attributes" }
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }
}
