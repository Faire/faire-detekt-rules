package com.faire.detekt.rules

import dev.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class NestedWithIsBannedTest {
  private lateinit var rule: NestedWithIsBanned

  @BeforeEach
  fun setup() {
    rule = NestedWithIsBanned()
  }

  @Test
  fun `directly nested with is flagged`() {
    val findings = rule.lint(
        """
        fun usage(payment: Payment) {
          with(payment) {
            println(state)
            with(instrumentLink) {
              println(active)
            }
          }
        }
        """.trimIndent(),
    )

    assertThat(findings).hasSize(1)
    assertThat(findings.single().entity.location.source.line).isEqualTo(4)
    assertThat(findings.single().message).isEqualTo(
        "Nested with calls are banned. Use a named variable for the inner receiver instead.",
    )
  }

  @Test
  fun `with nested through another lambda is flagged`() {
    val findings = rule.lint(
        """
        fun usage(items: List<Item>) {
          assertEvent(items) { eventItems ->
            with(eventItems[1]) {
              println(eventItemType)
              with(metadata as Metadata) {
                println(previousState)
              }
            }
          }
        }
        """.trimIndent(),
    )

    assertThat(findings).hasSize(1)
  }

  @Test
  fun `with nested inside a forEach is flagged`() {
    val findings = rule.lint(
        """
        fun usage(order: Order) {
          with(order) {
            items.forEach { item ->
              with(item) {
                println(quantity)
              }
            }
          }
        }
        """.trimIndent(),
    )

    assertThat(findings).hasSize(1)
  }

  @Test
  fun `each nested level is flagged`() {
    val findings = rule.lint(
        """
        fun usage(a: A) {
          with(a) {
            with(b) {
              with(c) {
                println(d)
              }
            }
          }
        }
        """.trimIndent(),
    )

    assertThat(findings).hasSize(2)
  }

  @Test
  fun `sibling with blocks are not flagged`() {
    val findings = rule.lint(
        """
        fun usage(a: A, b: B) {
          with(a) {
            println(x)
            println(y)
          }
          with(b) {
            println(x)
            println(y)
          }
        }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `with in the receiver argument of another with is not flagged`() {
    val findings = rule.lint(
        """
        fun usage(a: A) {
          with(with(a) { b }) {
            println(x)
            println(y)
          }
        }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `with in a local function inside a with is flagged`() {
    val findings = rule.lint(
        """
        fun usage(a: A) {
          with(a) {
            fun check(b: B) {
              with(b) {
                println(x)
              }
            }
            check(b)
          }
        }
        """.trimIndent(),
    )

    assertThat(findings).hasSize(1)
  }

  @Test
  fun `with in a local object inside a with is flagged`() {
    val findings = rule.lint(
        """
        fun usage(a: A) {
          with(a) {
            val checker = object : Runnable {
              override fun run() {
                with(b) {
                  println(x)
                }
              }
            }
            checker.run()
          }
        }
        """.trimIndent(),
    )

    assertThat(findings).hasSize(1)
  }

  @Test
  fun `nested with using lambdas inside parentheses is flagged`() {
    val findings = rule.lint(
        """
        fun usage(a: A) {
          with(a, {
            with(b, {
              println(x)
            })
          })
        }
        """.trimIndent(),
    )

    assertThat(findings).hasSize(1)
  }

  @Test
  fun `nested with using a labeled lambda is flagged`() {
    val findings = rule.lint(
        """
        fun usage(a: A) {
          with(a) outer@{
            with(b) {
              println(this@outer)
            }
          }
        }
        """.trimIndent(),
    )

    assertThat(findings).hasSize(1)
  }

  @Test
  fun `nested with imported under an alias is flagged`() {
    val findings = rule.lint(
        """
        import kotlin.with as using

        fun usage(a: A) {
          using(a) {
            using(b) {
              println(x)
            }
          }
        }
        """.trimIndent(),
    )

    assertThat(findings).hasSize(1)
  }

  @Test
  fun `with called on a receiver inside a with is not flagged`() {
    val findings = rule.lint(
        """
        fun usage(a: A, actionScoper: ActionScoper) {
          with(a) {
            actionScoper.with(seedData) {
              println(x)
            }
            actionScoper?.with(seedData) {
              println(y)
            }
          }
        }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `with that passes a block variable inside a with is not flagged`() {
    val findings = rule.lint(
        """
        fun usage(a: A, block: B.() -> Unit) {
          with(a) {
            with(b, block)
          }
        }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `functions and properties named with are not flagged`() {
    val findings = rule.lint(
        """
        class Thing {
          val with = 1
          fun with(): Int = with
        }

        fun usage(a: A, thing: Thing) {
          with(a) {
            println(thing.with())
            println(thing.with)
          }
        }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }
}
