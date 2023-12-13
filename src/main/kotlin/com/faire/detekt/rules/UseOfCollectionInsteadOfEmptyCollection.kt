package com.faire.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt.Companion.FIVE_MINS
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.astReplace
import org.jetbrains.kotlin.resolve.calls.util.getCalleeExpressionIfAny

/**
 * Instead of using `emptyMap()`, `emptyList()` or `emptySet()` we should use `mapOf()`, `listOf()`, or `setOf()`.
 *
 * This code convention exists for the purpose of consistency.
 */
internal class UseOfCollectionInsteadOfEmptyCollection(config: Config = Config.empty) : Rule(config) {
  override val issue: Issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.Warning,
      description = "replace emptySet(), emptyList(), or emptyMap() with setOf(), listOf(), or mapOf() respectively",
      debt = FIVE_MINS,
  )

  override fun visitCallExpression(expression: KtCallExpression) {
    super.visitCallExpression(expression)

    val callee = expression.getCalleeExpressionIfAny() ?: return

    if (callee.text in setOf("emptyList", "emptySet", "emptyMap")) {
      report(
          CodeSmell(
              issue = issue,
              entity = Entity.from(expression),
              message = issue.description,
          ),
      )

      withAutoCorrect {
        when (callee.text) {
          "emptyList" -> callee.astReplace(KtPsiFactory(callee).createExpression("listOf"))
          "emptySet" -> callee.astReplace(KtPsiFactory(callee).createExpression("setOf"))
          "emptyMap" -> callee.astReplace(KtPsiFactory(callee).createExpression("mapOf"))
        }
      }
    }
  }
}
