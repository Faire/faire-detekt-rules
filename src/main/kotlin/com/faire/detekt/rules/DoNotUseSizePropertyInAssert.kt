package com.faire.detekt.rules

import com.faire.detekt.utils.isAssertThat
import com.faire.detekt.utils.usesSizeProperty
import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

/**
 * In a test, we should always use the hasSize() assertion method instead of  comparing the size property to some value.
 *
 * One exception to using hasSize() is that it should not be used with an argument of zero. Use isEmpty() instead.
 * See [DoNotUseHasSizeForEmptyListInAssert].
 *
 * This is a convention we have chosen in order to keep our test assertions consistent.
 *
 * Good example:
 *
 * ```
 * fun `test usage`() {
 *     assertThat(myCollection).hasSize(3)
 * }
 * ```
 *
 * Bad example:
 *
 * ```
 * fun `test usage`() {
 *     assertThat(myCollection.size).isEqualTo(3)
 * }
 * ```
 */
internal class DoNotUseSizePropertyInAssert(config: Config = Config.empty) : Rule(config) {
  override val issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.Style,
      description = "Do not use size property in assertion, use hasSize() instead.",
      debt = Debt.FIVE_MINS,
  )

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return
    val receiverExpression = expression.receiverExpression

    if (!receiverExpression.isAssertThat()) return

    val selectorExpressionText = selectorExpression.referenceExpression()?.text
    if (selectorExpressionText != "isEqualTo" && selectorExpressionText != "isZero") return

    val assertExpression = receiverExpression as? KtCallExpression ?: return
    val isEqualToOrIsZeroExpression = selectorExpression as? KtCallExpression ?: return

    if (assertExpression.usesSizeProperty() && isEqualToOrIsZeroExpression.numericComparison()) {
      report(
          CodeSmell(
              issue = issue,
              entity = Entity.from(expression),
              message = issue.description,
          ),
      )
    }
  }
}

private fun KtCallExpression.numericComparison(): Boolean {
  if (firstChild.text == "isZero") {
    return true
  } else if (valueArguments.size != 1) {
    return false
  }

  val stringToCheck = valueArguments.single().text.trimEnd('L')

  return stringToCheck.toLongOrNull() != null
}
