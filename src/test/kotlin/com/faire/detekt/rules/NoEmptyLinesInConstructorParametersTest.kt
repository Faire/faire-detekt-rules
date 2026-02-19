package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRuleTest
import io.gitlab.arturbosch.detekt.test.assertThat
import io.gitlab.arturbosch.detekt.test.lint
import org.junit.jupiter.api.Test

internal class NoEmptyLinesInConstructorParametersTest : AutoCorrectRuleTest<NoEmptyLinesInConstructorParameters>(
    { NoEmptyLinesInConstructorParameters(it) },
) {

  @Test
  fun `single-line constructor has no violations`() {
    assertNoViolations(
        """
            class Person(val firstName: String, val lastName: String)
            """.trimIndent(),
    )
  }

  @Test
  fun `multi-line constructor with no blank lines has no violations`() {
    assertNoViolations(
        """
            class Person(
              val firstName: String,
              val lastName: String,
              val age: Int,
            )
            """.trimIndent(),
    )
  }

  @Test
  fun `blank line between two parameters is removed by autocorrect`() {
    assertLintAndFormat(
        codeToLint = """
            class Person(
              val firstName: String,

              val lastName: String,
              val age: Int,
            )
            """.trimIndent(),
        expectedPostFormattedCode = """
            class Person(
              val firstName: String,
              val lastName: String,
              val age: Int,
            )
            """.trimIndent(),
        issueDescription = rule.issue.description,
    )
  }

  @Test
  fun `multiple blank lines between parameters are removed by autocorrect`() {
    assertLintAndFormat(
        codeToLint = """
            class Person(
              val firstName: String,


              val lastName: String,
              val age: Int,
            )
            """.trimIndent(),
        expectedPostFormattedCode = """
            class Person(
              val firstName: String,
              val lastName: String,
              val age: Int,
            )
            """.trimIndent(),
        issueDescription = rule.issue.description,
    )
  }

  @Test
  fun `trailing blank line before closing parenthesis is removed by autocorrect`() {
    assertLintAndFormat(
        codeToLint = """
            class Person(
              val firstName: String,
              val lastName: String,
              val age: Int,

            )
            """.trimIndent(),
        expectedPostFormattedCode = """
            class Person(
              val firstName: String,
              val lastName: String,
              val age: Int,
            )
            """.trimIndent(),
        issueDescription = rule.issue.description,
    )
  }

  @Test
  fun `leading blank line after opening parenthesis is removed by autocorrect`() {
    assertLintAndFormat(
        codeToLint = """
            class Person(

              val firstName: String,
              val lastName: String,
              val age: Int,
            )
            """.trimIndent(),
        expectedPostFormattedCode = """
            class Person(
              val firstName: String,
              val lastName: String,
              val age: Int,
            )
            """.trimIndent(),
        issueDescription = rule.issue.description,
    )
  }

  @Test
  fun `secondary constructor with blank line is detected`() {
    assertLintAndFormat(
        codeToLint = """
            class Person {
              constructor(
                firstName: String,

                lastName: String,
              )
            }
            """.trimIndent(),
        expectedPostFormattedCode = """
            class Person {
              constructor(
                firstName: String,
                lastName: String,
              )
            }
            """.trimIndent(),
        issueDescription = rule.issue.description,
    )
  }

  @Test
  fun `function parameter list with blank line has no violation`() {
    assertNoViolations(
        """
            fun createPerson(
              firstName: String,

              lastName: String,
            ) {
              TODO()
            }
            """.trimIndent(),
    )
  }

  @Test
  fun `constructor with annotations on parameters has no false positives`() {
    assertNoViolations(
        """
            class Service @Inject constructor(
              @Named("api") val apiClient: ApiClient,
              @Named("db") val database: Database,
              val logger: Logger,
            )
            """.trimIndent(),
    )
  }

  @Test
  fun `constructor with default values spanning lines has no false positives`() {
    assertNoViolations(
        """
            class Config(
              val timeout: Long = 30_000L,
              val retries: Int = 3,
              val enabled: Boolean = true,
            )
            """.trimIndent(),
    )
  }

  @Test
  fun `empty line in data class constructor is removed by autocorrect`() {
    assertLintAndFormat(
        codeToLint = """
            data class Person(
              val firstName: String,

              val lastName: String,
              val age: Int,
            )
            """.trimIndent(),
        expectedPostFormattedCode = """
            data class Person(
              val firstName: String,
              val lastName: String,
              val age: Int,
            )
            """.trimIndent(),
        issueDescription = rule.issue.description,
    )
  }

  @Test
  fun `no-autocorrect mode reports violations but does not modify code`() {
    val findings = rule.lint(
        """
            class Person(
              val firstName: String,

              val lastName: String,
            )
            """.trimIndent(),
    )

    assertThat(findings).hasSize(1)
    assertThat(findings.first()).hasMessage(rule.issue.description)
  }

  @Test
  fun `multiple blank lines in different locations are all removed`() {
    assertLintAndFormat(
        codeToLint = """
            class Person(

              val firstName: String,

              val lastName: String,

              val age: Int,

            )
            """.trimIndent(),
        expectedPostFormattedCode = """
            class Person(
              val firstName: String,
              val lastName: String,
              val age: Int,
            )
            """.trimIndent(),
        issueDescription = rule.issue.description,
    )
  }

  @Test
  fun `whitespace-only lines between parameters are detected and removed`() {
    assertLintAndFormat(
        codeToLint = """
            class Person(
              val firstName: String,
              ${"  "}
              val lastName: String,
              ${"\t"}
              val age: Int,
            )
            """.trimIndent(),
        expectedPostFormattedCode = """
            class Person(
              val firstName: String,
              val lastName: String,
              val age: Int,
            )
            """.trimIndent(),
        issueDescription = rule.issue.description,
    )
  }

  @Test
  fun `comment-only lines between parameters are allowed`() {
    assertNoViolations(
        """
            class Person(
              val firstName: String,
              // Full name components
              val lastName: String,
              val age: Int,
            )
            """.trimIndent(),
    )
  }
}
