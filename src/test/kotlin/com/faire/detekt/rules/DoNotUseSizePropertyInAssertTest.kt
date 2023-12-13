package com.faire.detekt.rules

import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val ISSUE_DESCRIPTION = "Do not use size property in assertion, use hasSize() instead."

internal class DoNotUseSizePropertyInAssertTest {
  private lateinit var rule: DoNotUseSizePropertyInAssert

  @BeforeEach
  fun setup() {
    rule = DoNotUseSizePropertyInAssert()
  }

  @Test
  fun `collection size is caught`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(someCollection.size).isEqualTo(1)
          }
        """.trimIndent(),
    )

    assertThat(findings.map { it.message }).containsExactlyInAnyOrder(ISSUE_DESCRIPTION)
  }

  @Test
  fun `collection size is okay`() {
    val findings = rule.lint(
        """
          fun usage() {
            val size = someCollection.size
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `size property is okay when isEqual is not a number`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(sizingQuantities.first().size).isEqualTo("S")
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `size property is caught when isEqual is a number`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(sizingQuantities.first().size).isEqualTo(0)
          }
        """.trimIndent(),
    )

    assertThat(findings.map { it.message }).containsExactlyInAnyOrder(ISSUE_DESCRIPTION)
  }

  @Test
  fun `size property is okay when isEqual has a size`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(sizingQuantities.first().size).isEqualTo(someCollection.size)
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `sizesSummary is okay`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(flashSalePrepack.sizesSummary).isEqualTo("1–2–0–0 (S–XL)")
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `collection returned from function is caught`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(someCollection().size).isEqualTo(3)
          }
        """.trimIndent(),
    )

    assertThat(findings.map { it.message }).containsExactlyInAnyOrder(ISSUE_DESCRIPTION)
  }

  @Test
  fun `isGreaterThan is okay`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(someCollection.size).isGreaterThan(1)
            assertThat(otherValue).isEqualTo(5)
            assertThat(otherValue.property).isEqualTo(5)
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `expression containing size is okay`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(durationWeeks - instalments.size).isEqualTo(1)
            assertThat(durationWeeks + instalments.size).isEqualTo(1)
            assertThat(durationWeeks * instalments.size).isEqualTo(1)
            assertThat(durationWeeks / instalments.size).isEqualTo(1)
            assertThat(durationWeeks % instalments.size).isEqualTo(1)
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `size only is caught`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(size).isEqualTo(1)
          }
        """.trimIndent(),
    )

    assertThat(findings.map { it.message }).containsExactlyInAnyOrder(ISSUE_DESCRIPTION)
  }

  @Test
  fun `size property is caught when using isZero`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(someCollection.size).isZero()
          }
        """.trimIndent(),
    )

    assertThat(findings.map { it.message }).containsExactlyInAnyOrder(ISSUE_DESCRIPTION)
  }

  @Test
  fun `size property is caught when isEqualTo is a long`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(someCollection.size).isEqualTo(5L)
          }
        """.trimIndent(),
    )

    assertThat(findings.map { it.message }).containsExactlyInAnyOrder(ISSUE_DESCRIPTION)
  }

  @Test
  fun `size property is not caught when isEqualTo is L`() {
    val findings = rule.lint(
        """
          fun `test usage`() {
            assertThat(size).isEqualTo("L")
          }
        """.trimIndent(),
    )

    assertThat(findings.map { it.message }).isEmpty()
  }
}
