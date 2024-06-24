package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRuleTest
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DoNotAssertIsEqualOnTheResultOfSingleTest : AutoCorrectRuleTest<DoNotAssertIsEqualOnTheResultOfSingle> (
    { DoNotAssertIsEqualOnTheResultOfSingle(it) },
) {
    @Test
    fun `asserting isEqualTo does not produce an error if there's no call to single()`() {
        assertNoViolations(
            """
          fun foo() {
            assertThat(someValue).isEqualTo(someValue)
          }
        """.trimIndent(),
        )
    }

    @Test
    fun `asserting isEqualTo does not produce an error if it's not called directly on the result of single()`() {
        assertNoViolations(
            """
          fun foo() {
            assertThat(someList.single().someMethod()).isEqualTo(someValue)
            assertThat(someList.single().somePeroperty).isEqualTo(someValue)
          }
        """.trimIndent(),
        )
    }

    @Test
    fun `asserting isEqualTo on the result of single() would produce an error`() {
        val findings = rule.lint(
            """
          fun foo() {
            assertThat(bar.baz.qux.single()).isEqualTo(someValue)
          }
        """.trimIndent(),
        )
        assertThat(findings.single().id).isEqualTo("DoNotAssertIsEqualOnTheResultOfSingle")
    }
}
