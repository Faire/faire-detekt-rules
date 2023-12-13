package com.faire.detekt.rules

import com.faire.detekt.utils.isAssertThat
import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.astReplace
import org.jetbrains.kotlin.resolve.calls.util.getCalleeExpressionIfAny
import org.jetbrains.kotlin.resolve.calls.util.getValueArgumentsInParentheses

/**
 * In a test, we should always use isEmpty() instead of hasSize(0) when asserting on empty collections.
 *
 * This is a convention we have chosen in order to keep our test assertions consistent.
 *
 * Good example:
 *
 * ```
 * fun `test usage`() {
 *     assertThat(myCollection).isEmpty()
 * }
 * ```
 *
 * Bad example:
 *
 * ```
 * fun `test usage`() {
 *     assertThat(myCollection).hasSize(0)
 * }
 * ```
 */
internal class DoNotUseHasSizeForEmptyListInAssert(config: Config = Config.empty) : Rule(config) {
  override val issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.Style,
      description = "Do not call hasSize(0) on an empty collection, call isEmpty().",
      debt = Debt.FIVE_MINS,
  )

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return

    val hasSizeExpression = selectorExpression as? KtCallExpression ?: return

    if (!expression.receiverExpression.isAssertThat()) return

    val callee = expression.getCalleeExpressionIfAny() ?: return

    if (callee.text == "hasSize" && expression.isZero()) {
      report(
          CodeSmell(
              issue = issue,
              entity = Entity.from(expression),
              message = issue.description,
          ),
      )

      withAutoCorrect {
        val isEmptyExpression = KtPsiFactory(hasSizeExpression).createExpression("isEmpty()")
        hasSizeExpression.astReplace(isEmptyExpression)
      }
    }
  }
}

private fun KtDotQualifiedExpression.isZero(): Boolean {
  val size = (lastChild as? KtCallExpression)?.getValueArgumentsInParentheses()?.first()?.asElement()?.firstChild

  return size?.text == "0"
}
