package com.faire.detekt.rules

import dev.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ReturnValueOfLetMustBeUsedTest {

  private lateinit var rule: ReturnValueOfLetMustBeUsed

  @BeforeEach
  fun setup() {
    rule = ReturnValueOfLetMustBeUsed()
  }

  @Test
  fun `returning let expression produces no errors`() {
    val findings = rule.lint(
        """
        fun testFunc(): Int {
          return testFoo?.let { it -> it * 2 }
        }
      """.trimIndent(),
    )

    val multiLineFindings = rule.lint(
        """
        fun testFunc(): Int {
          return testFoo?.let { foo ->
            val boo = foo * 2
            boo * 2 
          }
        }
      """.trimIndent(),
    )

    assertThat(findings).isEmpty()
    assertThat(multiLineFindings).isEmpty()
  }

  @Test
  fun `single line function with let expression produces no errors`() {
    val findings = rule.lint(
        """
        fun fromMillis(millis: Long?): DateTime? = millis?.let { fromMillisSafe(it) }
      """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `assigning let expression produces no errors`() {
    val findings = rule.lint(
        """
        fun testFunc(): Int {
          val bar = testFoo?.let { it -> it * 2 }
        }
      """.trimIndent(),
    )

    val multiLineFindings = rule.lint(
        """
        fun testFunc(): Int {
          val bar = testFoo?.let { foo ->
            val boo = foo * 2
            boo * 2 
          }
        }
      """.trimIndent(),
    )

    assertThat(findings).isEmpty()
    assertThat(multiLineFindings).isEmpty()
  }

  @Test
  fun `assigning let expression to multiple values produces no errors`() {
    val findings = rule.lint(
        """
        fun testFunc(): Int {
          val (productLevelDiscounts, orderLevelDiscounts) = brandOrder.partitionDiscounts().let {
            Pair(it.productLevel, it.orderLevel)
          }
        }
      """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `assigning let to global expression produces no errors`() {
    val findings = rule.lint(
        """
        fun sync(): Int {
          language = event.language?.let { Language.valueOf(it) }
        }
      """.trimIndent(),
    )

    val multiLineFindings = rule.lint(
        """
        fun testFunc(): Int {
          bar = testFoo?.let { foo ->
            val boo = foo * 2
            boo * 2 
          }
        }
      """.trimIndent(),
    )

    assertThat(findings).isEmpty()
    assertThat(multiLineFindings).isEmpty()
  }

  @Test
  fun `assigning let return value as a parameter produces no errors`() {
    val findings = rule.lint(
        """
        fun sync(): Int {
          event.copy(stateOrProvince?.let { LegacyStateOrProvince.Companion.from(it) })
        }
      """.trimIndent(),
    )

    val multiLineFindings = rule.lint(
        """
        fun testFunc(): Int {
          event.copy(
            country = country ?: event.country,
            bestAddressState = stateOrProvince?.let { LegacyStateOrProvince.Companion.from(it) },
            shippingCountry = country,
            shippingStateOrProvince = stateOrProvince,
            addressCountry = country,
            addressStateOrProvince = stateOrProvince,
            bestLocation = bestLocation,
          )
        }
      """.trimIndent(),
    )

    assertThat(findings).isEmpty()
    assertThat(multiLineFindings).isEmpty()
  }

  @Test
  fun `let expression used in subsequent call produces no errors`() {
    val findings = rule.lint(
        """
        fun testFunc(): Int {
          val bar = testFoo?.let { it -> it * 2 }.nextFunc()
        }
      """.trimIndent(),
    )

    val multiLineFindings = rule.lint(
        """
        fun testFunc(): Int {
          val bar = testFoo?.let { foo ->
            val boo = foo * 2
            boo * 2 
          }.nextFunc()
        }
      """.trimIndent(),
    )

    assertThat(findings).isEmpty()
    assertThat(multiLineFindings).isEmpty()
  }

  @Test
  fun `let expression can be used in collections`() {
    val findings = rule.lint(
        """
        fun testFunc(): Int {
          val bar = listOfNotNull(x?.foo?.let { transform(it) } )
        }
      """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `let expression can be used in parameter default values`() {
    val findings = rule.lint(
        """
        fun testFunc(
            foo: Int?,
            bar: Int? = foo?.let { it + 2 },
        ): Int {
            return (foo ?: 0) + (bar ?: 0)
        }
      """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `let expression can be used in anonymous interface implementations`() {
    val findings = rule.lint(
        """
        private fun productOptionsComparator(
      variationIndexMap: Map<String, Map<String, Int>>,
  ): Comparator<ProductOption> = Comparator<ProductOption> { optionA, optionB ->
    for (i in 0 until optionA.variations.size) {
      val variationA = optionA.variations[i]
      val variationB = optionB.variations[i]

      val indexA = variationIndexMap[variationA.name]?.let(transform(variationA.value_))
      val indexB = variationIndexMap[variationB.name]?.get(variationB.value_)

      when {
        indexA == null && indexB == null -> return@Comparator 0
        indexA == null -> return@Comparator -1
        indexB == null -> return@Comparator 1
        indexA != indexB -> return@Comparator indexA - indexB
        // else continue
      }
    }
    return@Comparator 0
  }
      """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `let expression can be used in when block`() {
    val findings = rule.lint(
        """
        fun testFunc(): Int {
          return when (x) {
          is Case 1 -> x.field?.let { transform(it) }
        }
      """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `let expression used in subsequent call produces no errors even with nullability check`() {
    val findings = rule.lint(
        """
        fun testFunc(): Int {
          testFoo?.let { it -> it * 2 }?.nextFunc()
        }
      """.trimIndent(),
    )

    val multiLineFindings = rule.lint(
        """
        fun testFunc(): Int {
          interfaceWithMethod?.let { clazz ->
            clazz.declaredMethods
              .find { it.name == this.name }
              ?.getAnnotation(QueryParams::class.java)?.value
          }?.forEach {
          result.add(it.toEndpointArgument(javaToProto))
          }
        }
      """.trimIndent(),
    )

    assertThat(findings).isEmpty()
    assertThat(multiLineFindings).isEmpty()
  }

  @Test
  fun `let expression can be used as a when subject, condition, loop range or thrown exception`() {
    val findings = rule.lint(
        """
        fun testFunc(user: User, names: List<String>) {
          when (user.let { it.role }) {
            Role.ADMIN -> println("admin")
            else -> println("other")
          }
          if (user.let { it.isActive }) println("active")
          for (name in names.let { it.sorted() }) println(name)
          throw user.let { IllegalStateException(it.name) }
        }
      """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `let expression can be the value of an if, when or try that is used`() {
    val findings = rule.lint(
        """
        fun testFunc(user: User?, fallback: String): String {
          val name = if (user != null) {
            user.let { it.name }
          } else {
            fallback
          }
          val role = when (user) {
            null -> fallback
            else -> user.let { it.role.name }
          }
          return try {
            user!!.let { it.email }
          } catch (e: NullPointerException) {
            fallback.let { it.lowercase() }
          }
        }
      """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `let expression as the last statement of a lambda produces no errors`() {
    val findings = rule.lint(
        """
        fun testFunc(users: List<User?>): List<String?> {
          return users.map { user -> user?.let { it.name } }
        }
      """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `let expression used as last call produces an error`() {
    val findings = rule.lint(
        """
        fun testFunc(): Int {
          testFoo.transformation()?.let { it -> it * 2 }
        }
      """.trimIndent(),
    )

    val multiLineFindings = rule.lint(
        """
        fun testFunc(): Int {
          testFoo.transformation()
          ?.let { foo ->
            val boo = foo * 2
            boo * 2 
          }
        }
      """.trimIndent(),
    )

    assertThat(findings.single()).isNotNull()
    assertThat(multiLineFindings.single()).isNotNull()
  }

  @Test
  fun `let with named return produces an error if return value is not used`() {
    val findings = rule.lint(
        """
        fun testFunc(): Int {
          testFoo?.let { it -> return@testFunc it * 2 }
        }
      """.trimIndent(),
    )

    val multiLineFindings = rule.lint(
        """
        fun testFunc(): Int {
          testFoo?.let { foo ->
            val boo = foo * 2
            return@testFunc boo * 2 
          }
        }
      """.trimIndent(),
    )

    assertThat(findings.single()).isNotNull()
    assertThat(multiLineFindings.single()).isNotNull()
  }

  @Test
  fun `let where the return value is not used produces an error`() {
    val findings = rule.lint(
        """
        fun testFunc(): Int {
          testFoo?.let { it -> it.returnField }
          return 0
        }
      """.trimIndent(),
    )

    val multiLineFindings = rule.lint(
        """
        fun testFunc(): Int {
          testFoo?.let { foo ->
            val boo = foo * 2
            boo.returnField * 2 
          }
        }
      """.trimIndent(),
    )

    assertThat(findings.single()).isNotNull()
    assertThat(multiLineFindings.single()).isNotNull()
  }

  @Test
  fun `let whose value is discarded inside a lambda or object produces an error`() {
    val findings = rule.lint(
        """
        val handler = object : Handler {
          override fun handle(name: String?) {
            name?.let { println(it) }
            println("handled")
          }
        }

        fun testFunc(users: List<User?>): List<String> {
          users.forEach { user ->
            user?.let { println(it) }
            println("done")
          }
          return buildList {
            for (user in users) {
              user?.let { add(it.name) }
            }
          }
        }
      """.trimIndent(),
    )

    assertThat(findings.map { it.entity.ktElement.text }).containsExactly(
        "let { println(it) }",
        "let { println(it) }",
        "let { add(it.name) }",
    )
  }
}
