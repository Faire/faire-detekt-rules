package com.faire.detekt.rules

import dev.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val ISSUE_DESCRIPTION = "With block receiver should have multiple actions"

internal class WithReceiverShouldHaveMultipleActionsTest {
  private lateinit var rule: WithReceiverShouldHaveMultipleActions

  @BeforeEach
  fun setUp() {
    rule = WithReceiverShouldHaveMultipleActions()
  }

  @Test
  fun `with() having a single call to receiver is flagged`() {
    val findings = rule.lint(
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
    assertThat(findings.first().message).isEqualTo(ISSUE_DESCRIPTION)
  }

  @Test
  fun `with() having a multiple calls to receiver is not flagged`() {
    val findings = rule.lint(
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
