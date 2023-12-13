package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRuleTest
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class PreventBannedImportsTest : AutoCorrectRuleTest<PreventBannedImports>(
    {
      PreventBannedImports(
          TestConfig(
              "autoCorrect" to it.valueOrNull<Boolean>("autoCorrect")!!,
              "withAlternatives" to listOf(
                  "com.google.inject.Singleton=javax.inject.Singleton",
              ),
              "withoutAlternatives" to listOf("com.faire.madeUp.ForTesting"),
          ),
      )
    },
) {
  @Test
  fun `valid import emits no findings`() {
    val findings = rule.lint(
        """
        import com.faire.foo
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `google Singleton import emits finding with replacement`() {
    assertLintAndFormat(
        """
        import com.google.inject.Singleton
        """.trimIndent(),
        """
        import javax.inject.Singleton
        """.trimIndent(),
        issueDescription = "Replace com.google.inject.Singleton import with javax.inject.Singleton",
    )
  }

  @Test
  fun `can report imports without replacements configured`() {
    val findings = rule.lint(
        """
        import com.faire.madeUp.ForTesting
        """.trimIndent(),
    )

    assertThat(findings).hasSize(1)
    assertThat(findings.single().message)
        .isEqualTo("Do not import com.faire.madeUp.ForTesting")
  }

  @Test
  fun `sub-imports are reported correctly with replacement`() {
    assertLintAndFormat(
        """
        import com.google.inject.Singleton.Foo
        """.trimIndent(),
        """
        import javax.inject.Singleton.Foo
        """.trimIndent(),
        issueDescription = "Replace com.google.inject.Singleton import with javax.inject.Singleton",
    )
  }

  @Test
  fun `allowed imports that prefix disallowed prefixes are not reported`() {
    val findings = rule.lint(
        """
        import com.faire.madeUp.ForTesting2
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `disallowed subimports of allowed parent import are not reported`() {
    val findings = rule.lint(
        """
        import com.faire.madeUp
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `combination imports are handled correctly`() {
    assertLintAndFormat(
        """
        import com.google.inject.Singleton
        com.faire.madeUp.ForTesting
        """.trimIndent(),
        """
        import javax.inject.Singleton
        com.faire.madeUp.ForTesting
        """.trimIndent(),
        issueDescription = "Replace com.google.inject.Singleton import with javax.inject.Singleton",
    )
  }
}
