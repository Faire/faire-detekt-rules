package com.faire.detekt.rules

import dev.detekt.api.Finding
import dev.detekt.api.modifiedText
import dev.detekt.test.TestConfig
import dev.detekt.test.junit.KotlinCoreEnvironmentTest
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.KotlinAnalysisApiEngine
import dev.detekt.test.utils.KotlinEnvironmentContainer
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.config.languageVersionSettings
import org.junit.jupiter.api.Test

private const val ASSERTJ_STUB = """
package org.assertj.core.api

object Assertions {
  fun <T> assertThat(actual: Iterable<T>?): IterableAssert<T> = TODO()
  fun assertThat(actual: Any?): ObjectAssert = TODO()
}

object AssertionsForClassTypes {
  fun assertThat(actual: Boolean): ObjectAssert = TODO()
  fun <T> assertThat(actual: T): ObjectAssert = TODO()
}

class IterableAssert<T> {
  fun isEmpty() {}
  fun isNotEmpty(): IterableAssert<T> = this
  fun hasSize(expected: Int): IterableAssert<T> = this
  fun allSatisfy(requirements: (T) -> Unit): IterableAssert<T> = this
  fun allMatch(predicate: (T) -> Boolean): IterableAssert<T> = this
  fun anyMatch(predicate: (T) -> Boolean): IterableAssert<T> = this
  fun noneMatch(predicate: (T) -> Boolean): IterableAssert<T> = this
}

class ObjectAssert {
  fun isTrue(): ObjectAssert = this
  fun isFalse(): ObjectAssert = this
  fun isNotNull(): ObjectAssert = this
  fun isEmpty() {}
  fun isEqualTo(expected: Any?): ObjectAssert = this
}
"""

private const val TRUTH_STUB = """
package com.google.common.truth

object Truth {
  fun assertThat(actual: Boolean): BooleanSubject = TODO()
}

class BooleanSubject {
  fun isTrue() {}
}
"""

@KotlinCoreEnvironmentTest
internal class UseMatchAssertionsOnCollectionsTest(private val env: KotlinEnvironmentContainer) {
  @Test
  fun `all is true and filterNot is empty are replaced with allMatch`() {
    assertLintAndFormat(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers.all { it % 2 == 1 }).isTrue()
        }
        """,
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers).allMatch { it % 2 == 1 }
        }
        """,
        "Use assertThat(collection).allMatch { predicate } instead of " +
            "assertThat(collection.all { predicate }).isTrue()",
    )
    assertLintAndFormat(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers.filterNot { it % 2 == 1 }).isEmpty()
        }
        """,
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers).allMatch { it % 2 == 1 }
        }
        """,
        "Use assertThat(collection).allMatch { predicate } instead of " +
            "assertThat(collection.filterNot { predicate }).isEmpty()",
    )
  }

  @Test
  fun `any is true, none is false and filter is not empty are replaced with anyMatch`() {
    assertLintAndFormat(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers.any { it % 2 == 1 }).isTrue()
        }
        """,
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers).anyMatch { it % 2 == 1 }
        }
        """,
        "Use assertThat(collection).anyMatch { predicate } instead of " +
            "assertThat(collection.any { predicate }).isTrue()",
    )
    assertLintAndFormat(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers.none { it % 2 == 1 }).isFalse()
        }
        """,
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers).anyMatch { it % 2 == 1 }
        }
        """,
        "Use assertThat(collection).anyMatch { predicate } instead of " +
            "assertThat(collection.none { predicate }).isFalse()",
    )
    assertLintAndFormat(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers.filter { it % 2 == 1 }).isNotEmpty()
        }
        """,
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers).anyMatch { it % 2 == 1 }
        }
        """,
        "Use assertThat(collection).anyMatch { predicate } instead of " +
            "assertThat(collection.filter { predicate }).isNotEmpty()",
    )
  }

  @Test
  fun `none is true, any is false and filter is empty are replaced with noneMatch`() {
    assertLintAndFormat(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers.none { it % 2 == 1 }).isTrue()
        }
        """,
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers).noneMatch { it % 2 == 1 }
        }
        """,
        "Use assertThat(collection).noneMatch { predicate } instead of " +
            "assertThat(collection.none { predicate }).isTrue()",
    )
    assertLintAndFormat(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers.any { it % 2 == 1 }).isFalse()
        }
        """,
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers).noneMatch { it % 2 == 1 }
        }
        """,
        "Use assertThat(collection).noneMatch { predicate } instead of " +
            "assertThat(collection.any { predicate }).isFalse()",
    )
    assertLintAndFormat(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers.filter { it % 2 == 1 }).isEmpty()
        }
        """,
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers).noneMatch { it % 2 == 1 }
        }
        """,
        "Use assertThat(collection).noneMatch { predicate } instead of " +
            "assertThat(collection.filter { predicate }).isEmpty()",
    )
  }

  @Test
  fun `any iterable receiver is reported`() {
    listOf("Set<Int>", "Collection<Int>", "Iterable<Int>", "MutableList<Int>", "java.util.ArrayDeque<Int>")
        .forEach { type ->
          assertLintAndFormat(
              """
              import org.assertj.core.api.Assertions.assertThat

              fun test(numbers: $type) {
                assertThat(numbers.all { it > 0 }).isTrue()
              }
              """,
              """
              import org.assertj.core.api.Assertions.assertThat

              fun test(numbers: $type) {
                assertThat(numbers).allMatch { it > 0 }
              }
              """,
              "Use assertThat(collection).allMatch { predicate } instead of " +
                  "assertThat(collection.all { predicate }).isTrue()",
          )
        }
  }

  @Test
  fun `receivers that are not iterable are not reported`() {
    assertNoViolations(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: Map<Int, Int>, letters: String, sequence: Sequence<Int>, array: IntArray) {
          assertThat(numbers.all { (key, value) -> key < value }).isTrue()
          assertThat(numbers.filter { (key, value) -> key < value }).isEmpty()
          assertThat(letters.any { it.isDigit() }).isFalse()
          assertThat(sequence.none { it > 0 }).isTrue()
          assertThat(array.filter { it > 0 }).isEmpty()
        }
        """,
    )
  }

  @Test
  fun `paths are not reported`() {
    assertNoViolations(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(path: java.nio.file.Path) {
          assertThat(path.all { it.toString().isNotEmpty() }).isTrue()
        }
        """,
    )
  }

  @Test
  fun `member functions named like the collection functions are not reported`() {
    assertNoViolations(
        """
        import org.assertj.core.api.Assertions.assertThat

        class Bag(items: List<Int>) : Iterable<Int> by items {
          fun all(predicate: (Int) -> Boolean): Boolean = false
        }

        fun test(bag: Bag) {
          assertThat(bag.all { it > 0 }).isTrue()
        }
        """,
    )
  }

  @Test
  fun `predicates that are not lambdas are passed in parentheses`() {
    assertLintAndFormat(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun isOdd(number: Int): Boolean = number % 2 == 1

        fun test(numbers: List<Int>) {
          assertThat(numbers.all(::isOdd)).isTrue()
        }
        """,
        """
        import org.assertj.core.api.Assertions.assertThat

        fun isOdd(number: Int): Boolean = number % 2 == 1

        fun test(numbers: List<Int>) {
          assertThat(numbers).allMatch(::isOdd)
        }
        """,
        "Use assertThat(collection).allMatch { predicate } instead of " +
            "assertThat(collection.all { predicate }).isTrue()",
    )
    assertLintAndFormat(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>, isOdd: (Int) -> Boolean) {
          assertThat(numbers.filter(isOdd)).isEmpty()
        }
        """,
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>, isOdd: (Int) -> Boolean) {
          assertThat(numbers).noneMatch(isOdd)
        }
        """,
        "Use assertThat(collection).noneMatch { predicate } instead of " +
            "assertThat(collection.filter { predicate }).isEmpty()",
    )
  }

  @Test
  fun `named predicate argument drops the name`() {
    assertLintAndFormat(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>, isOdd: (Int) -> Boolean) {
          assertThat(numbers.any(predicate = isOdd)).isTrue()
        }
        """,
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>, isOdd: (Int) -> Boolean) {
          assertThat(numbers).anyMatch(isOdd)
        }
        """,
        "Use assertThat(collection).anyMatch { predicate } instead of " +
            "assertThat(collection.any { predicate }).isTrue()",
    )
  }

  @Test
  fun `lambda in parentheses becomes a trailing lambda`() {
    assertLintAndFormat(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers.none({ it > 0 })).isTrue()
        }
        """,
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers).noneMatch { it > 0 }
        }
        """,
        "Use assertThat(collection).noneMatch { predicate } instead of " +
            "assertThat(collection.none { predicate }).isTrue()",
    )
  }

  @Test
  fun `chained receivers are kept`() {
    assertLintAndFormat(
        """
        import org.assertj.core.api.Assertions.assertThat

        class Item(val price: Int)
        class Order(val items: List<Item>)

        fun test(order: Order) {
          assertThat(order.items.map { it.price }.filterNot { it > 0 }).isEmpty()
        }
        """,
        """
        import org.assertj.core.api.Assertions.assertThat

        class Item(val price: Int)
        class Order(val items: List<Item>)

        fun test(order: Order) {
          assertThat(order.items.map { it.price }).allMatch { it > 0 }
        }
        """,
        "Use assertThat(collection).allMatch { predicate } instead of " +
            "assertThat(collection.filterNot { predicate }).isEmpty()",
    )
  }

  @Test
  fun `assertions inside with are reported`() {
    assertLintAndFormat(
        """
        import org.assertj.core.api.Assertions.assertThat

        class Item(val price: Int)
        class Order(val items: List<Item>)

        fun test(order: Order) {
          with(order) {
            assertThat(items.any { it.price > 0 }).isTrue()
          }
        }
        """,
        """
        import org.assertj.core.api.Assertions.assertThat

        class Item(val price: Int)
        class Order(val items: List<Item>)

        fun test(order: Order) {
          with(order) {
            assertThat(items).anyMatch { it.price > 0 }
          }
        }
        """,
        "Use assertThat(collection).anyMatch { predicate } instead of " +
            "assertThat(collection.any { predicate }).isTrue()",
    )
  }

  @Test
  fun `return to the predicate label is reported but not auto-corrected`() {
    assertLintedButNotFormatted(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(
              numbers.filter {
                if (it < 0) return@filter false
                it % 2 == 1
              },
          ).isEmpty()
        }
        """,
    )
  }

  @Test
  fun `return from the enclosing function is reported but not auto-corrected`() {
    assertLintedButNotFormatted(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(
              numbers.all {
                if (it < 0) return
                it > 0
              },
          ).isTrue()
        }
        """,
    )
    assertLintedButNotFormatted(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<List<Int>>) {
          assertThat(
              numbers.any { row ->
                row.forEach { if (it < 0) return@test }
                row.isEmpty()
              },
          ).isFalse()
        }
        """,
    )
  }

  @Test
  fun `return inside a function declared in the predicate is auto-corrected`() {
    assertLintAndFormat(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers.all(fun(number: Int): Boolean { return number > 0 })).isTrue()
        }
        """,
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers).allMatch(fun(number: Int): Boolean { return number > 0 })
        }
        """,
        "Use assertThat(collection).allMatch { predicate } instead of " +
            "assertThat(collection.all { predicate }).isTrue()",
    )
  }

  @Test
  fun `multi-line predicates are re-indented`() {
    assertLintAndFormat(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(
              numbers.filter {
                val odd = it % 2 == 1
                odd && it > 3
              },
          ).isEmpty()
        }
        """,
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers).noneMatch {
            val odd = it % 2 == 1
            odd && it > 3
          }
        }
        """,
        "Use assertThat(collection).noneMatch { predicate } instead of " +
            "assertThat(collection.filter { predicate }).isEmpty()",
    )
    assertLintAndFormat(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers.filter {
            val odd = it % 2 == 1
            odd && it > 3
          }).isEmpty()
        }
        """,
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers).noneMatch {
            val odd = it % 2 == 1
            odd && it > 3
          }
        }
        """,
        "Use assertThat(collection).noneMatch { predicate } instead of " +
            "assertThat(collection.filter { predicate }).isEmpty()",
    )
  }

  @Test
  fun `multi-line predicates with a multi-line string keep their indentation`() {
    val quotes = "\"\"\""
    assertLintAndFormat(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(names: List<String>) {
          assertThat(
              names.any {
                it == $quotes
                  multi-line
                $quotes
              },
          ).isTrue()
        }
        """,
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(names: List<String>) {
          assertThat(names).anyMatch {
                it == $quotes
                  multi-line
                $quotes
              }
        }
        """,
        "Use assertThat(collection).anyMatch { predicate } instead of " +
            "assertThat(collection.any { predicate }).isTrue()",
    )
  }

  @Test
  fun `assertions with more assertions chained after them are not reported`() {
    assertNoViolations(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers.filter { it > 0 }).isNotEmpty().allSatisfy { assertThat(it).isNotNull() }
          assertThat(numbers.filter { it > 0 }).isNotEmpty().hasSize(2)
          assertThat(numbers.any { it > 0 }).isTrue().isNotNull()
          assertThat(numbers.none { it > 0 }).isFalse()?.isNotNull()
        }
        """,
    )
  }

  @Test
  fun `predicates that call suspend functions are not reported`() {
    assertNoViolations(
        """
        import org.assertj.core.api.Assertions.assertThat

        suspend fun exists(id: Int): Boolean = true

        suspend fun test(ids: List<Int>, isValid: suspend (Int) -> Boolean) {
          assertThat(ids.all { exists(it) }).isTrue()
          assertThat(ids.filter { isValid(it) }).isEmpty()
        }
        """,
    )
  }

  @Test
  fun `assertThat from other libraries is not reported`() {
    assertNoViolations(
        """
        import com.google.common.truth.Truth.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers.all { it > 0 }).isTrue()
        }
        """,
    )
  }

  @Test
  fun `assertThat from AssertionsForClassTypes is not reported`() {
    assertNoViolations(
        """
        import org.assertj.core.api.AssertionsForClassTypes.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers.all { it > 0 }).isTrue()
        }
        """,
    )
  }

  @Test
  fun `assertions without a match assertion equivalent are not reported`() {
    assertNoViolations(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers.all { it > 0 }).isFalse()
          assertThat(numbers.filterNot { it > 0 }).isNotEmpty()
          assertThat(numbers.any { it > 0 }).isEqualTo(true)
          assertThat(numbers.filter { it > 0 }).hasSize(2)
          assertThat(numbers.filter { it > 0 }.map { it * 2 }).isEmpty()
        }
        """,
    )
  }

  @Test
  fun `implicit receivers, safe calls and calls outside of assertThat are not reported`() {
    assertNoViolations(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>, maybeNumbers: List<Int>?) {
          with(numbers) {
            assertThat(all { it > 0 }).isTrue()
          }
          assertThat(maybeNumbers?.all { it > 0 }).isTrue()
          check(numbers.all { it > 0 })
          val allPositive = numbers.all { it > 0 }
        }
        """,
    )
  }

  @Test
  fun `findings are not auto-corrected when auto-correct is disabled`() {
    val ktFile = KotlinAnalysisApiEngine.compile(
        """
        import org.assertj.core.api.Assertions.assertThat

        fun test(numbers: List<Int>) {
          assertThat(numbers.all { it > 0 }).isTrue()
        }
        """.trimIndent(),
        listOf(ASSERTJ_STUB),
    )
    val findings = UseMatchAssertionsOnCollections(TestConfig("autoCorrect" to false))
        .visitFile(ktFile, env.configuration.languageVersionSettings)

    assertThat(findings).hasSize(1)
    assertThat(ktFile.modifiedText).isNull()
  }

  private fun assertLintAndFormat(
      @Language("kotlin") code: String,
      @Language("kotlin") expectedCode: String,
      expectedMessage: String,
  ) {
    assertThat(lint(code.trimIndent())).extracting<String> { it.message }.containsExactly(expectedMessage)

    val correctedCode = autoCorrect(code.trimIndent())
    assertThat(correctedCode).isEqualTo(expectedCode.trimIndent())
    assertThat(lint(correctedCode!!)).isEmpty()
  }

  private fun assertLintedButNotFormatted(@Language("kotlin") code: String) {
    assertThat(lint(code.trimIndent())).hasSize(1)
    assertThat(autoCorrect(code.trimIndent())).isNull()
  }

  private fun assertNoViolations(@Language("kotlin") code: String) {
    assertThat(lint(code.trimIndent())).isEmpty()
  }

  private fun lint(code: String): List<Finding> =
    UseMatchAssertionsOnCollections().lintWithContext(env, code, ASSERTJ_STUB, TRUTH_STUB)

  private fun autoCorrect(code: String): String? {
    val ktFile = KotlinAnalysisApiEngine.compile(code, listOf(ASSERTJ_STUB, TRUTH_STUB))
    UseMatchAssertionsOnCollections(TestConfig("autoCorrect" to true))
        .visitFile(ktFile, env.configuration.languageVersionSettings)
    return ktFile.modifiedText
  }
}
