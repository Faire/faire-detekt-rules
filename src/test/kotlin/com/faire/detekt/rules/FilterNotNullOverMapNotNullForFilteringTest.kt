package com.faire.detekt.rules

import com.faire.detekt.rules.FilterNotNullOverMapNotNullForFiltering
import com.faire.detekt.utils.AutoCorrectRuleTest
import org.junit.jupiter.api.Test

internal class FilterNotNullOverMapNotNullForFilteringTest :
    AutoCorrectRuleTest<FilterNotNullOverMapNotNullForFiltering>(
        { FilterNotNullOverMapNotNullForFiltering(it) },
    ) {

  @Test
  fun `should detect and auto-correct mapNotNull { it } to filterNotNull()`() {
    assertLintAndFormat(
        """
                fun test() {
                    val result = listOf(1, null, 2).mapNotNull { it }
                }
            """.trimIndent(),
        """
                fun test() {
                    val result = listOf(1, null, 2).filterNotNull()
                }
            """.trimIndent(),
        issueDescription = "Replace mapNotNull { it } with filterNotNull()",
    )
  }

  @Test
  fun `should detect and auto-correct chained mapNotNull { it }`() {
    assertLintAndFormat(
        """
                fun test() {
                    val result = listOf(1, null, 2).filter { it != 3 }.mapNotNull { it }.map { it * 2 }
                }
            """.trimIndent(),
        """
                fun test() {
                    val result = listOf(1, null, 2).filter { it != 3 }.filterNotNull().map { it * 2 }
                }
            """.trimIndent(),
        issueDescription = "Replace mapNotNull { it } with filterNotNull()",
    )
  }

  @Test
  fun `should not detect mapNotNull with transformation`() {
    assertNoViolations(
        """
                fun test() {
                    val result = listOf(1, null, 2).mapNotNull { it?.toString() }
                }
            """.trimIndent(),
    )
  }

  @Test
  fun `should not detect filterNotNull`() {
    assertNoViolations(
        """
                fun test() {
                    val result = listOf(1, null, 2).filterNotNull()
                }
            """.trimIndent(),
    )
  }

  @Test
  fun `should not detect regular map`() {
    assertNoViolations(
        """
                fun test() {
                    val result = listOf(1, 2, 3).map { it }
                }
            """.trimIndent(),
    )
  }

  @Test
  fun `should detect and auto-correct with complex receiver expression`() {
    assertLintAndFormat(
        """
                fun test() {
                    val result = getSomeList().filter { it.isValid() }.mapNotNull { it }
                }
            """.trimIndent(),
        """
                fun test() {
                    val result = getSomeList().filter { it.isValid() }.filterNotNull()
                }
            """.trimIndent(),
        issueDescription = "Replace mapNotNull { it } with filterNotNull()",
    )
  }
}
