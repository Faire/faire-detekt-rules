package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRuleTest
import org.junit.jupiter.api.Test

private const val RULE_DESCRIPTION =
    "containsOnly should be used instead of asserting isEqual on the result of single()"

internal class DoNotAssertIsEqualOnTheResultOfSingleTest : AutoCorrectRuleTest<DoNotAssertIsEqualOnTheResultOfSingle>(
    { DoNotAssertIsEqualOnTheResultOfSingle(it) },
) {
    @Test
    fun `asserting isEqualTo does not produce an error if there's no call to single()`() {
        assertNoViolationsAndNotFormatted(
            """
          fun foo() {
            assertThat(someValue).isEqualTo(someValue)
          }
        """.trimIndent(),
        )
    }

    @Test
    fun `asserting isEqualTo does not produce an error if it's not called directly on the result of single()`() {
        assertNoViolationsAndNotFormatted(
            """
          fun foo() {
            assertThat(someList.single().someMethod()).isEqualTo(someValue)
            assertThat(someList.single().somePeroperty).isEqualTo(someValue)
          }
        """.trimIndent(),
        )
    }

    @Test
    fun `asserting isEqualTo does not produce an error if a predicate is passed`() {
        assertNoViolationsAndNotFormatted(
            """
          fun foo() {
            assertThat(someList.single { it > 0 }).isEqualTo(someValue)
          }
        """.trimIndent(),
        )
    }

    @Test
    fun `asserting isEqualTo on the result of single() would produce an error`() {
        assertLintAndFormat(
            """
            fun foo() {
                assertThat(bar.baz.qux.single()).isEqualTo(someValue)
            }
            """.trimIndent(),
            """
            fun foo() {
                assertThat(bar.baz.qux).containsOnly(someValue)
            }
            """.trimIndent(),
            issueDescription = RULE_DESCRIPTION,
        )
    }
}
