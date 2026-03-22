package com.faire.detekt.utils

import dev.detekt.api.Config
import dev.detekt.api.Rule
import dev.detekt.test.TestConfig
import dev.detekt.test.lint
import dev.detekt.test.utils.compileContentForTest
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language

internal abstract class AutoCorrectRuleTest<T : Rule>(
    factory: (config: Config) -> T,
    additionalConfig: List<Pair<String, Any>> = listOf(),
) {
  val rule = factory(TestConfig("autoCorrect" to false, *additionalConfig.toTypedArray()))
  private val autoCorrectRule = factory(TestConfig("autoCorrect" to true, *additionalConfig.toTypedArray()))

  fun assertLintAndFormat(
      @Language("kotlin")
      codeToLint: String,
      @Language("kotlin")
      expectedPostFormattedCode: String,
      issueDescription: String,
      fileName: String = "Test.kt",
  ) {
    val code = compileContentForTest(codeToLint, fileName)
    val findings = autoCorrectRule.lint(code)
    assertThat(findings)
        .`as`("Expected issues to be: '$issueDescription'")
        .extracting<String> { it.message }
        .allMatch { it == issueDescription }
    assertThat(code.text).isEqualTo(expectedPostFormattedCode)

    val postFormattedCode = compileContentForTest(code.text, fileName)
    assertThat(autoCorrectRule.lint(postFormattedCode)).isEmpty()
  }

  fun assertNoViolationsAndNotFormatted(
      @Language("kotlin")
      codeToLint: String,
      fileName: String = "Test.kt",
  ) {
    val code = compileContentForTest(codeToLint, fileName)
    val findings = autoCorrectRule.lint(code)
    assertThat(code.text).isEqualTo(codeToLint)
    assertThat(findings).isEmpty()
  }

  // Lint the supplied code and ensure there are no detekted violations
  fun assertNoViolations(
      @Language("kotlin")
      codeToLint: String,
  ) {
    val code = compileContentForTest(codeToLint)
    assertThat(rule.lint(code)).isEmpty()
  }
}
