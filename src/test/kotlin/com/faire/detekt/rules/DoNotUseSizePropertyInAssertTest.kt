package com.faire.detekt.rules

import io.github.detekt.test.utils.KotlinCoreEnvironmentWrapper
import io.github.detekt.test.utils.createEnvironment
import io.gitlab.arturbosch.detekt.api.Finding
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val ISSUE_DESCRIPTION = "Do not use size property in assertion, use hasSize() instead."

internal class DoNotUseSizePropertyInAssertTest {
  private lateinit var rule: DoNotUseSizePropertyInAssert
  private lateinit var envWrapper: KotlinCoreEnvironmentWrapper

  @BeforeEach
  fun setup() {
    rule = DoNotUseSizePropertyInAssert()
    envWrapper = createEnvironment(listOf())
  }

  @AfterEach
  fun tearDown() {
    envWrapper.dispose()
  }

  @Test
  fun `collection size is invalid in assertion when coming from a collection or map`() {
    assertInvalid(
        """
        val list = listOf(1, 2, 3)
        assertThat(list.size).isEqualTo(1)
        """,
    )

    assertInvalid(
        """
        val set = setOf(1, 2, 3)
        assertThat(set.size).isEqualTo(1)
        """,
    )

    assertInvalid(
        """
        val map = mapOf("foo" to 1)
        assertThat(map.size).isEqualTo(1)
        """,
    )

    assertValid(
        """
        val s = Sizeable(size = 1)
        assertThat(s.size).isEqualTo(1)
        """,
    )
  }

  @Test
  fun `collection size is valid outside of assertions`() {
    assertValid(
        """
        val list = listOf(1, 2, 3)
        val size = list.size"
        """,
    )
  }

  @Test
  fun `size property is only invalid when isEqual is a number`() {
    assertInvalid(
        """
        val list = listOf(1, 2, 3)
        assertThat(list.size).isEqualTo(1)
        """,
    )

    assertInvalid(
        """
        val list = listOf(1, 2, 3)
        assertThat(list.size).isEqualTo(3L)
        """,
    )

    assertValid(
        """
        val list = listOf(1, 2, 3)
        assertThat(list.size).isEqualTo("S")
        """,
    )

    assertValid(
        """
        val list = listOf<Sizeable>()
        assertThat(list.first().size).isEqualTo("S")
        """,
    )
  }

  @Test
  fun `size property is invalid when isEqual is a number`() {
    assertInvalid(
        """
        val sizingQuantities = listOf<Map<String, Int>>()
        assertThat(sizingQuantities.first().size).isEqualTo(0)
        """,
    )
  }

  @Test
  fun `size property on assert is valid when comparing to another size property`() {
    assertValid(
        """
        val list = listOf<Map<String, Int>>()
        assertThat(list.first().size).isEqualTo(list.size)
        """,
    )
  }

  @Test
  fun `size property on other classes is valid`() {
    assertValid(
        """
        val list = listOf<Sizeable>()
        assertThat(list.first().size).isEqualTo(0)
        """,
    )
    assertInvalid(
        """
        val list = listOf<Sizeable>()
        assertThat(list.size).isEqualTo(0)
        """,
    )
  }

  @Test
  fun `collection returned from function is invalid`() {
    assertInvalid(
        """
        fun makeList(): List<Int> = listOf(1, 2, 3)
        assertThat(makeList().size).isEqualTo(3)
        """,
    )
  }

  @Test
  fun `non-isEqualTo assertions are valid`() {
    assertValid(
        """
        val list = listOf(1, 2, 3)
        assertThat(list.size).isGreaterThan(1)
        """,
    )
    assertValid(
        """
        val list = listOf(1, 2, 3)
        assertThat(list.size).isLessOrEqualTo(1)
        """,
    )
  }

  @Test
  fun `expression containing size is okay`() {
    assertValid(
        """
        val list = listOf(1, 2, 3)
        val durationWeeks = 2
        assertThat(durationWeeks - list.size).isEqualTo(1)
        assertThat(durationWeeks + list.size).isEqualTo(1)
        assertThat(durationWeeks * list.size).isEqualTo(1)
        assertThat(durationWeeks / list.size).isEqualTo(1)
        assertThat(durationWeeks % list.size).isEqualTo(1)
        """,
    )
  }

  @Test
  fun `size from a with context`() {
    assertInvalid(
        """
        with(listOf(1, 2, 3)) {
            assertThat(size).isEqualTo(1)
        }
        """,
    )

    assertValid(
        """
        with(Sizeable(size = 1)) {
            assertThat(size).isEqualTo(1)
        }
        """,
    )
  }

  @Test
  fun `size from a extension function context`() {
    assertInvalid(
        """
        fun List<*>.foo() {
            assertThat(size).isEqualTo(1)
        }
        """,
    )
    assertInvalid(
        """
        fun Map<*, *>.foo() {
            assertThat(size).isEqualTo(1)
        }
        """,
    )
    assertValid(
        """
        fun Sizeable.foo() {
            assertThat(size).isEqualTo(1)
        }
        """,
    )
  }

  @Test
  fun `size property is caught when using isZero`() {
    assertInvalid(
        """
        val list = listOf(1, 2, 3) 
        assertThat(list.size).isZero()
        """,
    )
  }

  @Test
  fun `size property is caught when isEqualTo is a long`() {
    assertInvalid("assertThat(listOf(1, 2, 3).size).isEqualTo(5L)")
  }

  private fun assertValid(code: String) = assertThat(runLint(code)).isEmpty()
  private fun assertInvalid(code: String) = assertThat(runLint(code).single().message).isEqualTo(ISSUE_DESCRIPTION)

  private fun runLint(code: String): List<Finding> {
    return rule.compileAndLintWithContext(
        envWrapper.env,
        """
        import kotlin.collections.List
        import kotlin.collections.Map

        data class Sizeable(val size: Int)

        fun `test usage`() {
            ${code.trimIndent()}
        }
        """.trimIndent(),
    )
  }
}
