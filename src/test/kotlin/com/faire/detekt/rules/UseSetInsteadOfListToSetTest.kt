package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRuleTest
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val ISSUE_DESCRIPTION = "Use set() instead of list().toSet()"

internal class UseSetInsteadOfListToSetTest : AutoCorrectRuleTest<UseSetInsteadOfListToSet>({
  UseSetInsteadOfListToSet(
      it,
  )
}) {

  @Test
  fun `list() followed by toSet() is flagged`() {
    assertLintAndFormat(
        """
          fun foo() {
            return session.createCriteria<DbTest>
              .list()
              .toSet()
          }
        """.trimIndent(),
        """
          fun foo() {
            return session.createCriteria<DbTest>
              .set()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )

    assertLintAndFormat(
        """
          fun foo() {
            return session.createCriteria<DbTest>.list().toSet()
          }
        """.trimIndent(),
        """
          fun foo() {
            return session.createCriteria<DbTest>.set()
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `list() without toSet() is not flagged`() {
    val findings = rule.lint(
        """
          fun foo() {
            return session.createCriteria<DbTest>
              .list()
          }
        """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }

  @Test
  fun `toSet() without list() is not flagged`() {
    val findings = rule.lint(
        """
          fun foo(myList: List<String>) {
            return myList.toSet()
          }
        """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }

  @Test
  fun `toSet() not immediately following list() is not flagged`() {
    val findings = rule.lint(
        """
          fun foo() {
            return session.createCriteria<DbTest>
              .list()
              .map { it.token }
              .toSet()
          }
        """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }

  @Test
  fun `list() with parameters and toSet() not flagged`() {
    val findings = rule.lint(
        """
          fun foo() {
            return session.createCriteria<DbTest>
              .list(batchSize = 500)
              .toSet()
          }
        """.trimIndent(),
    )
    assertThat(findings).isEmpty()
  }

  @Test
  fun `list() with toSet() not at end of chain flagged`() {
    assertLintAndFormat(
        """
          fun foo() {
            return session.createCriteria<DbTest>
              .list()
              .toSet()
              .map { it.token }
          }
        """.trimIndent(),
        """
          fun foo() {
            return session.createCriteria<DbTest>
              .set()
              .map { it.token }
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }
}
