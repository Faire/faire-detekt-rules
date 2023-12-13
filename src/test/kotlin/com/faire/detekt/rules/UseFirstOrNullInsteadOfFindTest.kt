package com.faire.detekt.rules

import io.github.detekt.test.utils.KotlinCoreEnvironmentWrapper
import io.github.detekt.test.utils.createEnvironment
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class UseFirstOrNullInsteadOfFindTest {
  private lateinit var rule: UseFirstOrNullInsteadOfFind
  private lateinit var envWrapper: KotlinCoreEnvironmentWrapper

  @BeforeEach
  fun setup() {
    rule = UseFirstOrNullInsteadOfFind()
    envWrapper = createEnvironment(listOf())
  }

  @AfterEach
  fun tearDown() {
    envWrapper.dispose()
  }

  @Test
  fun `find() is flagged`() {
    assertThat(
        rule.compileAndLintWithContext(
            envWrapper.env,
            """
              import kotlin.collections.List
              
              fun foo() {
                val testList = listOf(0).find()
                val testString = "test".find()
              }
            """.trimIndent(),
        ),
    ).hasSize(2)
  }

  @Test
  fun `find{ } is flagged`() {
    assertThat(
        rule.compileAndLintWithContext(
            envWrapper.env,
            """
              import kotlin.collections.List
              
              fun foo() {
                val testList = listOf(0).find { it == 0 }
                val testString = "test".find { it == 's' }
              }
            """.trimIndent(),
        ),
    ).hasSize(2)
  }

  @Test
  fun `find() on unrelated type is not flagged`() {
    assertThat(
        rule.compileAndLintWithContext(
            envWrapper.env,
            """
              object TestQuery {
                fun find(
                    name: String
                ): String? {
                  return name
                }
              }
              
              fun foo() {
                val testQuery = TestQuery.find("test")
                val regex = "sample_Regex".toRegex().find("string")
              }
            """.trimIndent(),
        ),
    ).isEmpty()
  }

  @Test
  fun `firstOrNull() is not flagged`() {
    assertThat(
        rule.compileAndLintWithContext(
            envWrapper.env,
            """
              import kotlin.collections.List
              
              fun foo() {
                val testList = listOf(0).firstOrNull()
                val testString = "test".firstOrNull()
              }
            """.trimIndent(),
        ),
    ).isEmpty()
  }

  @Test
  fun `firstOrNull{ } is not flagged`() {
    assertThat(
        rule.compileAndLintWithContext(
            envWrapper.env,
            """
              import kotlin.collections.List
              
              fun foo() {
                val testList = listOf(0).firstOrNull { it == 0 }
                val testString = "test".firstOrNull { it == 's' }
              }
            """.trimIndent(),
        ),
    ).isEmpty()
  }
}
