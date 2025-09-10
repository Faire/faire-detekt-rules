package com.faire.detekt.rules

import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DoNotUseDirectReceiverReferenceInsideWithTest {
  private lateinit var rule: DoNotUseDirectReceiverReferenceInsideWith

  @BeforeEach
  fun setup() {
    rule = DoNotUseDirectReceiverReferenceInsideWith()
  }

  @Test
  fun `using the receiver within the with returns an error`() {
    val findings = rule.lint(
        """
        fun testFunc(): Int {
          with(foo) {
            assertThat(foo).isEqualTo("Bar")
          }
        }
      """.trimIndent(),
    )
    assertThat(findings.single().id).isEqualTo("DoNotUseDirectReceiverReferenceInsideWith")
  }

  @Test
  fun `using the receiver to access a property within the with returns an error`() {
    val findings = rule.lint(
        """
        fun testFunc(): Int {
          with(foo) {
            assertThat(foo.name).isEqualTo("Bar")
            assertThat(powerLevel).isGreaterThan(9000)
          }
        }
      """.trimIndent(),
    )
    assertThat(findings.single().id).isEqualTo("DoNotUseDirectReceiverReferenceInsideWith")
  }

  @Test
  fun `using the receiver within a nested with returns an error`() {
    val findings = rule.lint(
        """
        fun testFunc(): Int {
          with(foo) {
            with(bar) {
              assertThat(foo.name).isEqualTo("Bar")
            }
          }
        }
      """.trimIndent(),
    )
    assertThat(findings.single().id).isEqualTo("DoNotUseDirectReceiverReferenceInsideWith")
  }

  @Test
  fun `using the nested receiver within a nested with returns an error`() {
    val findings = rule.lint(
        """
        fun testFunc(): Int {
          with(foo) {
            with(bar) {
              assertThat(bar.name).isEqualTo("hello")
            }
          }
        }
      """.trimIndent(),
    )
    assertThat(findings.single().id).isEqualTo("DoNotUseDirectReceiverReferenceInsideWith")
  }

  @Test
  fun `not using the receiver withing the with returns no errors`() {
    val findings = rule.lint(
        """
        fun testFunc(): Int {
          with(foo) {
            assertThat(name).isEqualTo("Bar")
            assertThat(powerLevel).isGreaterThan(9000)
          }
        }
      """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }

  @Test
  fun `using a variable with a similar name as the receiver within the with returns no errors`() {
    val findings = rule.lint(
        """
        fun testFunc(): Int {
          with(foo) {
            assertThat(food).isEqualTo("Bar")
            assertThat(powerLevel).isGreaterThan(9000)
          }
        }
      """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }

  @Test
  fun `using a property with the same as the receiver within the with returns no errors`() {
    val findings = rule.lint(
        """
        fun testFunc(): Int {
          with(foo) {
            assertThat(food.foo).isEqualTo("Bar")
            assertThat(powerLevel).isGreaterThan(9000)
          }
        }
      """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }

  @Test
  fun `having a comment with the same as the receiver within the with returns no errors`() {
    val findings = rule.lint(
        """
        fun testFunc(): Int {
          with(foo) {
            // the food of foo
            assertThat(food).isEqualTo("Bar")
            assertThat(powerLevel).isGreaterThan(9000)
          }
        }
      """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }

  @Test
  fun `Not using a with has no problems`() {
    val findings = rule.lint(
        """
        fun testFunc(): Int {
          return testFoo?.let { foo ->
            val boo = foo.value * 2
            boo * 2 
          }
        }
      """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }

  @Test
  fun `using a named argument with the receiver name has no errors `() {
    val findings = rule.lint(
        """
        val request = with(entrepreneur) {
            ConfirmCustomerTypeRequest(
                companySearch = null,
                entrepreneur = EntrepreneurCustomerInfo(
                    companyName = Property(companyName),
                    ico = Property(ico.value),
                    dic = Property(dic?.value)
                )
            )
        }
        """.trimIndent()
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `using a parameter within a function has no errors`() {
    val findings = rule.lint(
        """
        class testing {
          fun testFunc(foo): Int {
            return foo.value
          }
        }
      """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }

  @Test
  fun `using a parameter within a function named with has no errors`() {
    val findings = rule.lint(
        """
        class testing {
          fun with(foo): Int {
            return foo.value
          }
        }
      """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }

  @Test
  fun `using a new variable named foo within a function named with has no errors`() {
    val findings = rule.lint(
        """
        class testing {
          fun with(foo): Int {
           val foo = bar
           return foo.value
          }
        }
      """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }

  @Test
  fun `should not detekt issues with an extension function named with`() {
    val findings = rule.lint(
        """
        fun `nesting scopes is not allowed`() {
          actionScoper.with(mapOf()) {
            assertThatThrownBy {
              actionScoper.with(mapOf()) {}
            }.hasMessageContaining("cannot begin an ActionScope on a thread that is already running in an action scope")
          }
        }
      """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }
}
