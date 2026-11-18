package com.faire.detekt.rules

import dev.detekt.test.junit.KotlinCoreEnvironmentTest
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.KotlinEnvironmentContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class DoNotAccessVisibleForTestingTest(private val env: KotlinEnvironmentContainer) {
  private lateinit var rule: DoNotAccessVisibleForTesting

  @BeforeEach
  fun setup() {
    rule = DoNotAccessVisibleForTesting()
  }

  // We do not want to produce duplicate findings when the symbol is later used.
  @Test
  fun `test importing test-only symbol`() {
    val findings = rule.lintWithContext(
        env,
        """
          package bar

          import foo.test

          fun bar() {}
        """,
        """
          package foo

          annotation class VisibleForTesting

          @VisibleForTesting
          fun test() {}
        """,
    )
    assertThat(findings).isEmpty()
  }

  @Test
  fun `test accessing test-only members`() {
    val findings = rule.lintWithContext(
        env,
        """
          package bar

          import foo.Foo

          fun bar() {
            val foo = Foo()
            foo.testMember()
            foo.testProperty
          }
        """,
        """
          package foo

          annotation class VisibleForTesting

          class Foo {
            @VisibleForTesting
            val testProperty = 42

            @VisibleForTesting
            fun testMember() {}
          }
        """,
    )
    assertThat(findings).hasSize(2)
  }

  @Test
  fun `test accessing test-only classes and constructors`() {
    val findings = rule.lintWithContext(
        env,
        """
          package bar

          import foo.FakeClock
          import foo.Scheduler

          fun bar() {
            val clock: FakeClock = FakeClock()
            Scheduler(clock)
            Scheduler()
          }
        """,
        """
          package foo

          annotation class VisibleForTesting

          interface Clock

          @VisibleForTesting
          class FakeClock : Clock

          class Scheduler @VisibleForTesting constructor(private val clock: Clock) {
            constructor() : this(object : Clock {})
          }
        """,
    )
    assertThat(findings.map { it.entity.ktElement.text }).containsExactly("FakeClock", "FakeClock", "Scheduler")
  }

  @Test
  fun `test accessing test-only members inherited from a generic class`() {
    val dependencies = arrayOf(
        """
          package foo

          annotation class VisibleForTesting

          open class Base<T> {
            @VisibleForTesting
            fun testMember(value: T): T = value
          }
        """,
        """
          package bar

          class Sub : foo.Base<String>()
        """,
    )

    val findingsInOtherPackage = rule.lintWithContext(
        env,
        """
          package bar

          fun bar(): String = Sub().testMember("value")
        """,
        *dependencies,
    )
    val findingsInDeclaringPackage = rule.lintWithContext(
        env,
        """
          package foo

          fun foo(): String = bar.Sub().testMember("value")
        """,
        *dependencies,
    )

    assertThat(findingsInOtherPackage.map { it.entity.ktElement.text }).containsExactly("testMember")
    assertThat(findingsInDeclaringPackage).isEmpty()
  }
}
