package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRuleTest
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val ISSUE_DESCRIPTION = "Do not use isOne(), use isEqualTo(1) instead."

internal class DoNotUseIsOneAssertionsTest : AutoCorrectRuleTest<DoNotUseIsOneAssertions>(
    { DoNotUseIsOneAssertions(it) },
) {
    @Test
    fun `using isOne causes failure`() {
        val findings = rule.lint(
            """
           fun `test usage`() {
               assertThat(products).isOne()
           }
        """.trimIndent(),
        )

        assertThat(findings.first().message).isEqualTo(ISSUE_DESCRIPTION)
    }
}
