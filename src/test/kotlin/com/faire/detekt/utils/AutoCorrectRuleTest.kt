package com.faire.detekt.utils

import io.github.detekt.test.utils.compileContentForTest
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.com.intellij.core.CoreFileTypeRegistry
import org.jetbrains.kotlin.com.intellij.openapi.application.ApplicationManager
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

internal abstract class AutoCorrectRuleTest<T : Rule>(
  factory: (config: Config) -> T,
  additionalConfig: List<Pair<String, Any>> = listOf(),
) {
  val rule = factory(TestConfig("autoCorrect" to false, *additionalConfig.toTypedArray()))
  private val autoCorrectRule = factory(TestConfig("autoCorrect" to true, *additionalConfig.toTypedArray()))
  private lateinit var mockApp: MockApplication

  @BeforeEach
  fun setup() {
    mockApp = initMockApplication()
  }

  @AfterEach
  fun tearDown() {
    mockApp.dispose()
  }

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

  // This sets up the Intellij "application" used for auto correction in non-unit
  // test mode. This prevents a bug where valid changes are considered
  // invalid, leading to a failure on CI
  private fun initMockApplication(): MockApplication {
    val disposable = Disposer.newDisposable()
    val mockApp = MockApplication(disposable)
    ApplicationManager.setApplication(mockApp, { CoreFileTypeRegistry() }, disposable)

    /*
     * For some reasons, I was getting:
     * Missing extension point: org.jetbrains.kotlin.com.intellij.treeCopyHandler in container com.faire.detekt.utils.MockApplication
     * when running the whole test suite on the class for InternalTestClass.kt. Something in the MockApplication
     * is not instantiation the treeCopyHandler extension point. This is a workaround to register it.
     */
    RegisterTreeCopyHandlerUtils.register()

    return mockApp
  }
}
