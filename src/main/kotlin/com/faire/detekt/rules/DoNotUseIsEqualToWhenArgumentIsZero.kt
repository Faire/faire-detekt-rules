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
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.astReplace
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

private val ZERO_TEXTS = setOf("0.0", "0", "0L", "0.0f", "0f", "0.0F", "0x0", "0b0")

/**
 * Use `isZero()` instead of `isEqualTo(0)` when asserting on a Number.
 *
 * ```
 * @Test
 * fun `check that there were no failures`() {
 *   // Good
 *   assertThat(failuresCount).isZero()
 *
 *   // Bad
 *   assertThat(failuresCount).isEqualTo(0)
 * }
 * ```
 */
internal class DoNotUseIsEqualToWhenArgumentIsZero(config: Config = Config.empty) : Rule(config) {
  override val issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.Style,
      description = "Do not use isEqualTo(0), use isZero() instead.",
      debt = Debt.FIVE_MINS,
  )

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return
    val receiverExpression = expression.receiverExpression

    val assertExpression = receiverExpression as? KtCallExpression ?: return
    val isEqualToExpression = selectorExpression as? KtCallExpression ?: return

    if (!receiverExpression.isAssertThat()) return

    if (selectorExpression.referenceExpression()?.text != "isEqualTo") return

    if (assertExpression.usesSizeProperty()) return

    if (isEqualToExpression.isComparingToZero()) {
      report(
          CodeSmell(
              issue = issue,
              entity = Entity.from(expression),
              message = issue.description,
          ),
      )

      withAutoCorrect {
        val isZeroExpression = KtPsiFactory(isEqualToExpression).createExpression("isZero()")
        isEqualToExpression.astReplace(isZeroExpression)
      }
    }
  }
}

private fun KtCallExpression.isComparingToZero(): Boolean {
  val argument = valueArguments.singleOrNull()?.text ?: return false

  return argument in ZERO_TEXTS
}
