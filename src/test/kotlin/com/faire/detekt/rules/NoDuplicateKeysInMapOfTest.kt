package com.faire.detekt.rules

import io.github.detekt.test.utils.compileContentForTest
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class NoDuplicateKeysInMapOfTest {
  private lateinit var rule: NoDuplicateKeysInMapOf

  @BeforeEach
  fun setup() {
    rule = NoDuplicateKeysInMapOf()
  }

  @Test
  fun `should not report when map has no duplicate keys`() {
    val ktFile = compileContentForTest(
      """
          val map = mapOf(
              "key1" to "value1",
              "key2" to "value2",
          )
        """.trimIndent(),
    )

    val findings = rule.lint(ktFile)
    assertThat(findings).isEmpty()
  }

  @Test
  fun `should report when map has duplicate keys`() {
    val ktFile = compileContentForTest(
      """
          val map = mapOf(
              "key1" to "value1",
              "key1" to "value2",
          )
        """.trimIndent(),
    )

    val findings = rule.lint(ktFile)
    assertThat(findings)
      .singleElement()
      .extracting { it.message }
      .isEqualTo("The key \"key1\" is duplicated in the map.")
  }

  @Test
  fun `should report for mutable map with duplicate keys`() {
    val ktFile = compileContentForTest(
      """
          val map = mutableMapOf(
              "key1" to "value1",
              "key1" to "value2",
          )
        """.trimIndent(),
    )

    val findings = rule.lint(ktFile)
    assertThat(findings)
      .singleElement()
      .extracting { it.message }
      .isEqualTo("The key \"key1\" is duplicated in the map.")
  }

  @Test
  fun `should report for duplicate keys when key is a variable`() {
    val ktFile = compileContentForTest(
      """
          fun key1 = "key1"
          val map = mapOf(
              key1 to "value1",
              key1 to "value2",
          )
        """.trimIndent(),
    )

    val findings = rule.lint(ktFile)
    assertThat(findings)
      .singleElement()
      .extracting { it.message }
      .isEqualTo("The key key1 is duplicated in the map.")
  }

  @Test
  fun `should not report for duplicate keys when key is a function call`() {
    val ktFile = compileContentForTest(
      """
          fun getKey() = "key1"
          val map = mapOf(
              getKey() to "value1",
              getKey() to "value2",
          )
        """.trimIndent(),
    )

    val findings = rule.lint(ktFile)
    assertThat(findings).isEmpty()
  }
}
