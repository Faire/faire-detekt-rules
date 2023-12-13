package com.faire.detekt.rules

import io.gitlab.arturbosch.detekt.test.lint
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
    with(findings.single()) {
      assertThat(id).isEqualTo("NoPairWithAmbiguousTypes")
      assertThat(message).isEqualTo("The function add has parameter pair which should be a class instead")
    }
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
    with(findings.single()) {
      assertThat(id).isEqualTo("NoPairWithAmbiguousTypes")
      assertThat(message).isEqualTo("The function add has parameter pair which should be a class instead")
    }
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
    with(findings.single()) {
      assertThat(id).isEqualTo("NoPairWithAmbiguousTypes")
      assertThat(message).isEqualTo("The function add has parameter pair which should be a class instead")
    }
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
    with(findings.single()) {
      assertThat(id).isEqualTo("NoPairWithAmbiguousTypes")
      assertThat(message).isEqualTo("The function add has parameter pair which should be a class instead")
    }
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
}
