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
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

/**
 * In a test, we should always use isTrue() instead of isEqualTo(true) and isFalse() instead of isEqualTo(false).
 *
 * This is a convention we have chosen in order to keep our test assertions consistent.
 *
 * Good example:
 *
 * ```
 * fun `test usage`() {
 *     assertThat(someValue).isTrue()
 *     assertThat(someValue).isFalse()
 * }
 * ```
 *
 * Bad example:
 *
 * ```
 * fun `test usage`() {
 *     assertThat(someValue).isEqualTo(true)
 *     assertThat(someValue).isEqualTo(false)
 * }
 * ```
 */
internal class AlwaysUseIsTrueOrIsFalse(config: Config = Config.empty) : Rule(config) {
  override val issue: Issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.Style,
      description = "Do not use isEqualTo(true) or isEqualTo(false), use isTrue() or isFalse()",
      debt = Debt.FIVE_MINS,
  )

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return
    val receiverExpression = expression.receiverExpression

    val isEqualToExpression = selectorExpression as? KtCallExpression ?: return

    if (!receiverExpression.isAssertThat()) return

    if (selectorExpression.referenceExpression()?.text != "isEqualTo") return

    if (isEqualToExpression.isComparingToTrue() || isEqualToExpression.isComparingToFalse()) {
      report(
          CodeSmell(
              issue = issue,
              entity = Entity.from(expression),
              message = issue.description,
          ),
      )

      withAutoCorrect {
        if (isEqualToExpression.isComparingToTrue()) {
          val isTrueExpression = KtPsiFactory(isEqualToExpression).createExpression("isTrue()")
          isEqualToExpression.astReplace(isTrueExpression)
        } else {
          val isFalseExpression = KtPsiFactory(isEqualToExpression).createExpression("isFalse()")
          isEqualToExpression.astReplace(isFalseExpression)
        }
      }
    }
  }
}

private fun KtCallExpression.isComparingToTrue(): Boolean {
  val argument = valueArguments.singleOrNull()?.text ?: return false

  return argument == "true"
}

private fun KtCallExpression.isComparingToFalse(): Boolean {
  val argument = valueArguments.singleOrNull()?.text ?: return false

  return argument == "false"
}
