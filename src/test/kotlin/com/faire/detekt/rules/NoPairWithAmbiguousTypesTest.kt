package com.faire.detekt.rules

import dev.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class NoPairWithAmbiguousTypesTest {
  private lateinit var rule: NoPairWithAmbiguousTypes

  @BeforeEach
  fun setUp() {
    rule = NoPairWithAmbiguousTypes()
  }

  @Test
  fun `invalid Pair(T, T) argument`() {
    val findings = rule.lint(
        """
          fun add(pair: Pair<Int, Int>): Int {
            return pair.first + pair.second
          }
        """.trimIndent(),
    )

    assertThat(findings).hasSize(1)
    assertThat(findings.single().message)
        .isEqualTo("The function add has parameter pair which should be a class instead")
  }

  @Test
  fun `invalid Pair(Any, T) argument`() {
    val findings = rule.lint(
        """
          fun add(pair: Pair<Any, Int>): Int {
            return (pair.first as Int) + pair.second
          }
        """.trimIndent(),
    )

    assertThat(findings).hasSize(1)
    assertThat(findings.single().message)
        .isEqualTo("The function add has parameter pair which should be a class instead")
  }

  @Test
  fun `invalid Pair with types T and nullable T argument`() {
    val findings = rule.lint(
        """
          fun add(pair: Pair<Int, Int?>): Int {
            return pair.first + pair.second!!
          }
        """.trimIndent(),
    )

    assertThat(findings).hasSize(1)
    assertThat(findings.single().message)
        .isEqualTo("The function add has parameter pair which should be a class instead")
  }

  @Test
  fun `invalid Pair with both nullable T argument`() {
    val findings = rule.lint(
        """
          fun add(pair: Pair<Int?, Int?>): Int {
            return pair.first!! + pair.second!!
          }
        """.trimIndent(),
    )

    assertThat(findings).hasSize(1)
    assertThat(findings.single().message)
        .isEqualTo("The function add has parameter pair which should be a class instead")
  }

  @Test
  fun `valid Pair(S, T) argument`() {
    val findings = rule.lint(
        """
          fun add(pair: Pair<String, Int>): Int {
            return (pair.first as Int) + pair.second
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `valid Pair(S, T) argument with generic type containing Any`() {
    val findings = rule.lint(
        """
          fun add(pair: Pair<CustomAny, Int>): Int {
            return (pair.first as Int) + pair.second
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `valid generic Pair argument`() {
    val findings = rule.lint(
        """
          fun add(pair: Pair<List<Int>, List<String>>): Int {
            return 42
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `valid Pair of different types with the same simple name`() {
    val findings = rule.lint(
        """
          fun link(tokens: Pair<Order.Token, Brand.Token>): Int {
            return 42
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `invalid Pair of identical function types`() {
    val findings = rule.lint(
        """
          fun run(callbacks: Pair<() -> Unit, () -> Unit>) {
            callbacks.first()
          }
        """.trimIndent(),
    )

    assertThat(findings.single().message)
        .isEqualTo("The function run has parameter callbacks which should be a class instead")
  }
}
