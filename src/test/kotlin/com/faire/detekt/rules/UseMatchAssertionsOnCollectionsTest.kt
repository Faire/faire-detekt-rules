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
fun <T> assertThat(actual: Iterable<T>?): IterableAssert<T> = TODO()
fun assertThat(actual: Any?): ObjectAssert = TODO()

class IterableAssert<T> {
  fun isEmpty() {}
  fun isNotEmpty() {}
  fun hasSize(expected: Int) {}
  fun allMatch(predicate: (T) -> Boolean) {}
  fun anyMatch(predicate: (T) -> Boolean) {}
  fun noneMatch(predicate: (T) -> Boolean) {}
}

class ObjectAssert {
  fun isTrue() {}
  fun isFalse() {}
  fun isEmpty() {}
  fun isEqualTo(expected: Any?) {}
}
"""

@KotlinCoreEnvironmentTest
internal class UseMatchAssertionsOnCollectionsTest(private val env: KotlinEnvironmentContainer) {
  @Test
  fun `all is true and filterNot is empty are replaced with allMatch`() {
    assertLintAndFormat(
        """
        fun test(numbers: List<Int>) {
          assertThat(numbers.all { it % 2 == 1 }).isTrue()
        }
        """,
        """
        fun test(numbers: List<Int>) {
          assertThat(numbers).allMatch { it % 2 == 1 }
        }
        """,
        "Use assertThat(collection).allMatch { predicate } instead of " +
            "assertThat(collection.all { predicate }).isTrue()",
    )
    assertLintAndFormat(
        """
        fun test(numbers: List<Int>) {
          assertThat(numbers.filterNot { it % 2 == 1 }).isEmpty()
        }
        """,
        """
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
        fun test(numbers: List<Int>) {
          assertThat(numbers.any { it % 2 == 1 }).isTrue()
        }
        """,
        """
        fun test(numbers: List<Int>) {
          assertThat(numbers).anyMatch { it % 2 == 1 }
        }
        """,
        "Use assertThat(collection).anyMatch { predicate } instead of " +
            "assertThat(collection.any { predicate }).isTrue()",
    )
    assertLintAndFormat(
        """
        fun test(numbers: List<Int>) {
          assertThat(numbers.none { it % 2 == 1 }).isFalse()
        }
        """,
        """
        fun test(numbers: List<Int>) {
          assertThat(numbers).anyMatch { it % 2 == 1 }
        }
        """,
        "Use assertThat(collection).anyMatch { predicate } instead of " +
            "assertThat(collection.none { predicate }).isFalse()",
    )
    assertLintAndFormat(
        """
        fun test(numbers: List<Int>) {
          assertThat(numbers.filter { it % 2 == 1 }).isNotEmpty()
        }
        """,
        """
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
        fun test(numbers: List<Int>) {
          assertThat(numbers.none { it % 2 == 1 }).isTrue()
        }
        """,
        """
        fun test(numbers: List<Int>) {
          assertThat(numbers).noneMatch { it % 2 == 1 }
        }
        """,
        "Use assertThat(collection).noneMatch { predicate } instead of " +
            "assertThat(collection.none { predicate }).isTrue()",
    )
    assertLintAndFormat(
        """
        fun test(numbers: List<Int>) {
          assertThat(numbers.any { it % 2 == 1 }).isFalse()
        }
        """,
        """
        fun test(numbers: List<Int>) {
          assertThat(numbers).noneMatch { it % 2 == 1 }
        }
        """,
        "Use assertThat(collection).noneMatch { predicate } instead of " +
            "assertThat(collection.any { predicate }).isFalse()",
    )
    assertLintAndFormat(
        """
        fun test(numbers: List<Int>) {
          assertThat(numbers.filter { it % 2 == 1 }).isEmpty()
        }
        """,
        """
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
              fun test(numbers: $type) {
                assertThat(numbers.all { it > 0 }).isTrue()
              }
              """,
              """
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
        fun isOdd(number: Int): Boolean = number % 2 == 1

        fun test(numbers: List<Int>) {
          assertThat(numbers.all(::isOdd)).isTrue()
        }
        """,
        """
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
        fun test(numbers: List<Int>, isOdd: (Int) -> Boolean) {
          assertThat(numbers.filter(isOdd)).isEmpty()
        }
        """,
        """
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
        fun test(numbers: List<Int>, isOdd: (Int) -> Boolean) {
          assertThat(numbers.any(predicate = isOdd)).isTrue()
        }
        """,
        """
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
        fun test(numbers: List<Int>) {
          assertThat(numbers.none({ it > 0 })).isTrue()
        }
        """,
        """
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
        class Item(val price: Int)
        class Order(val items: List<Item>)

        fun test(order: Order) {
          assertThat(order.items.map { it.price }.filterNot { it > 0 }).isEmpty()
        }
        """,
        """
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
        class Item(val price: Int)
        class Order(val items: List<Item>)

        fun test(order: Order) {
          with(order) {
            assertThat(items.any { it.price > 0 }).isTrue()
          }
        }
        """,
        """
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
  fun `assertions without a match assertion equivalent are not reported`() {
    assertNoViolations(
        """
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
    UseMatchAssertionsOnCollections().lintWithContext(env, code, ASSERTJ_STUB)

  private fun autoCorrect(code: String): String? {
    val ktFile = KotlinAnalysisApiEngine.compile(code, listOf(ASSERTJ_STUB))
    UseMatchAssertionsOnCollections(TestConfig("autoCorrect" to true))
        .visitFile(ktFile, env.configuration.languageVersionSettings)
    return ktFile.modifiedText
  }
}
