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

/**
 * Do not use property access syntax with assertion methods.
 *
 * Do not remove the parenthesis because these are assertions that might throw an exception so they should not be
 * treated as properties.
 *
 * good
 * `assertThat(foo).isTrue()`
 *
 * bad
 * `assertThat(foo).isTrue`
 */

internal class DoNotUsePropertyAccessInAssert(config: Config = Config.empty) : Rule(config) {
  override val issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.Style,
      description = "Do not use property access syntax with assertion methods. " +
          "Do not remove the parenthesis.",
      debt = Debt.FIVE_MINS,
  )

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return
    val receiverExpression = expression.receiverExpression
    if (receiverExpression !is KtCallExpression) return

    if (receiverExpression.isAssertThat() && selectorExpression !is KtCallExpression) {
      report(
          CodeSmell(
              issue = issue,
              entity = Entity.from(expression),
              message = issue.description,
          ),
      )

      withAutoCorrect {
        val withParenthesisExpression = KtPsiFactory(selectorExpression)
            .createExpression("${selectorExpression.text}()")
        selectorExpression.astReplace(withParenthesisExpression)
      }
    }
  }
}
