package com.faire.detekt.rules

import io.github.detekt.test.utils.KotlinCoreEnvironmentWrapper
import io.github.detekt.test.utils.createEnvironment
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val ISSUE_DESCRIPTION = "With block receiver should have multiple actions"

internal class WithReceiverShouldHaveMultipleActionsTest {
    private lateinit var rule: WithReceiverShouldHaveMultipleActions
    private lateinit var envWrapper: KotlinCoreEnvironmentWrapper

    @BeforeEach
    fun setUp() {
        rule = WithReceiverShouldHaveMultipleActions()
        envWrapper = createEnvironment(listOf())
    }

    @AfterEach
    fun tearDown() {
        envWrapper.dispose()
    }

    @Test
    fun `with() having a single call to receiver is flagged`() {
        val findings = rule.compileAndLintWithContext(
            envWrapper.env,
            """
            fun foo() {
                val receiver = "foobar"
                with(receiver) {
                    assertThat(length).isEqualTo(10)
                }
            }
        """.trimIndent(),
        )
        assertThat(findings).hasSize(1)
        with(findings.first().issue) {
            assertThat(id).isEqualTo("WithReceiverShouldHaveMultipleActions")
            assertThat(description).isEqualTo(ISSUE_DESCRIPTION)
            assertThat(severity).isEqualTo(Severity.Style)
        }
    }

    @Test
    fun `with() having a multiple calls to receiver is not flagged`() {
        val findings = rule.compileAndLintWithContext(
            envWrapper.env,
            """
            fun foo() {
                val receiver = "foobar"
                with(receiver) {
                    assertThat(length).isEqualTo(10)
                    assertThat(length).isNotEqualTo(8)
                }
            }
        """.trimIndent(),
        )
        assertThat(findings).isEmpty()
    }
}
