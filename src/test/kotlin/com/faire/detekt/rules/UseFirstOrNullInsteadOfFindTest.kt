package com.faire.detekt.rules

import dev.detekt.test.junit.KotlinCoreEnvironmentTest
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.KotlinEnvironmentContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class UseFirstOrNullInsteadOfFindTest(private val env: KotlinEnvironmentContainer) {
  private lateinit var rule: UseFirstOrNullInsteadOfFind

  @BeforeEach
  fun setup() {
    rule = UseFirstOrNullInsteadOfFind()
  }

  @Test
  fun `find{ } is flagged on collections`() {
    assertThat(
        rule.lintWithContext(
            env,
            """
              import kotlin.collections.List

              fun foo() {
                val testList = listOf(0).find { true }
                val testString = "test".find { true }
              }
            """.trimIndent(),
        ),
    ).hasSize(2)
  }

  @Test
  fun `find{ } is flagged`() {
    assertThat(
        rule.lintWithContext(
            env,
            """
              import kotlin.collections.List

              fun foo() {
                val testList = listOf(0).find { it == 0 }
                val testString = "test".find { it == 's' }
              }
            """.trimIndent(),
        ),
    ).hasSize(2)
  }

  @Test
  fun `find{ } is flagged on nullable collection chain`() {
    assertThat(
        rule.lintWithContext(
            env,
            """
              data class Option(val id: Int)
              data class PaymentOptions(val items: List<Option>)

              fun foo(paymentOptions: PaymentOptions?, selectedOptionIds: Set<Int>) {
                val selectedPaymentOption = paymentOptions?.items?.find { opt -> opt.id in selectedOptionIds }
              }
            """.trimIndent(),
        ),
    ).hasSize(1)
  }

  @Test
  fun `find() on unrelated type is not flagged`() {
    assertThat(
        rule.lintWithContext(
            env,
            """
              object TestQuery {
                fun find(
                    name: String
                ): String? {
                  return name
                }
              }

              fun foo() {
                val testQuery = TestQuery.find("test")
                val regex = "sample_Regex".toRegex().find("string")
              }
            """.trimIndent(),
        ),
    ).isEmpty()
  }

  @Test
  fun `firstOrNull() is not flagged`() {
    assertThat(
        rule.lintWithContext(
            env,
            """
              import kotlin.collections.List

              fun foo() {
                val testList = listOf(0).firstOrNull()
                val testString = "test".firstOrNull()
              }
            """.trimIndent(),
        ),
    ).isEmpty()
  }

  @Test
  fun `firstOrNull{ } is not flagged`() {
    assertThat(
        rule.lintWithContext(
            env,
            """
              import kotlin.collections.List

              fun foo() {
                val testList = listOf(0).firstOrNull { it == 0 }
                val testString = "test".firstOrNull { it == 's' }
              }
            """.trimIndent(),
        ),
    ).isEmpty()
  }
}
