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

private val ONE_TEXTS = setOf("1.0", "1", "1L", "1.0f", "1f", "1.0F", "0x1", "0b1")

/**
 * Use `isOne()` instead of `isEqualTo(1)` when asserting on a Number.
 *
 * ```
 * @Test
 * fun `check that there was a single failure`() {
 *   // Good
 *   assertThat(failuresCount).isOne()
 *
 *   // Bad
 *   assertThat(failuresCount).isEqualTo(1)
 * }
 * ```
 */
internal class DoNotUseIsEqualToWhenArgumentIsOne(config: Config = Config.empty) : Rule(config) {
  override val issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.Style,
      description = "Do not use isEqualTo(1), use isOne() instead.",
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

    if (isEqualToExpression.isComparingToOne()) {
      report(
          CodeSmell(
              issue = issue,
              entity = Entity.from(expression),
              message = issue.description,
          ),
      )

      withAutoCorrect {
        val isOneExpression = KtPsiFactory(isEqualToExpression).createExpression("isOne()")
        isEqualToExpression.astReplace(isOneExpression)
      }
    }
  }
}

private fun KtCallExpression.isComparingToOne(): Boolean {
  val argument = valueArguments.singleOrNull()?.text ?: return false

  return argument in ONE_TEXTS
}
