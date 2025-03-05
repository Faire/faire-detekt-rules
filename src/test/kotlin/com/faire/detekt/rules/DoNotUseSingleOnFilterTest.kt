package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRuleTest
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val ISSUE_DESCRIPTION = "Do not use single() with filter { ... }, use single { ... } instead"

internal class DoNotUseSingleOnFilterTest : AutoCorrectRuleTest<DoNotUseSingleOnFilter>({
  DoNotUseSingleOnFilter(it)
}) {

  @Test
  fun `filter {} and single() consecutive call pair is caught and corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            invoices.items.filter { it.type }.single()
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            invoices.items.single { it.type }
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )

    assertLintAndFormat(
        """
          fun `test usage`() {
            products.filter { it.preorderable!! && it.name == "Test" }.single()
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            products.single { it.preorderable!! && it.name == "Test" }
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )

    assertLintAndFormat(
        """
          fun `test usage`() {
            dbBrandOrder.currencyConversionRates.asSequence()
            .filter { it.event == CREATED_BRAND_TO_FAIRE }
            .filter { setOf(it.fromCurrency, it.toCurrency) == setOf("USD", "EUR")}
            .single()!!.rate
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            dbBrandOrder.currencyConversionRates.asSequence()
            .filter { it.event == CREATED_BRAND_TO_FAIRE }
            .single { setOf(it.fromCurrency, it.toCurrency) == setOf("USD", "EUR")}!!.rate
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `filter {} and single() non-consecutive call pair is not caught`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            products.filter { it.preorderable!! }.map { it.options }.single()
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `variable with filter in name calling single() is not caught`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            filterOptions.single { it.name == "Test" }
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `filter with no lambda arguments followed by single() is caught and corrected`() {
    assertLintAndFormat(
        """
          fun `test usage`() {
            val predicate = { i: Int -> false }
            something.filter(predicate).single()
          }
        """.trimIndent(),
        """
          fun `test usage`() {
            val predicate = { i: Int -> false }
            something.single(predicate)
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `filter without single is not caught`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            products.filter { it.name == "Test" }
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `single without filter is not caught`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            products.single { it.name == "Test" }
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `parameterless filter invocation can have a following single call`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            products.filter().single { it.name == "Test" }
            products.filter().single()
            // for some made up filter method that takes two lambdas
            products.filter { it.name == "Test" } { it.description = "Test" }.single()
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }
}
