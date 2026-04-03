package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRule
import com.faire.detekt.utils.isAssertThat
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

/**
 * Do not assert isEqual on the result of single(). Instead, use containsOnly.
 *
 * Using .single() will throw an exception if the collection does not contain exactly one element.
 * The exception results in the assertion failure message being hidden, which makes debugging more difficult.
 *
 * ```
 * // Good
 * assertThat(listof("a")).containsOnly("a")
 *
 * // Bad
 * assertThat(listof("a").single()).isEqualTo("a")
 * ```
 */
internal class DoNotAssertIsEqualOnTheResultOfSingle(config: Config = Config.empty) :
    AutoCorrectRule(config, "use containsOnly() instead of asserting isEqual() on the result of single()") {

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val isEqualExpression = expression.selectorExpression ?: return
    val receiverExpression = expression.receiverExpression

    if (!receiverExpression.isAssertThat()) return
    if (isEqualExpression.referenceExpression()?.text != "isEqualTo") return

    val assertThatExpression = receiverExpression as? KtCallExpression ?: return
    val argumentForAssertThatExpression =
        assertThatExpression.valueArguments.singleOrNull()?.getArgumentExpression() ?: return
    if (!argumentForAssertThatExpression.callsSingleWithNoArgumentAtTheEnd()) return

    report(
        Finding(
            entity = Entity.from(expression),
            message = "containsOnly should be used instead of asserting isEqual on the result of single()",
        ),
    )

    if (autoCorrect) {
      // assertThat(x.single()).isEqualTo(y) -> assertThat(x).containsOnly(y)
      val collectionExpr = (argumentForAssertThatExpression as KtDotQualifiedExpression).receiverExpression.text
      val isEqualArgs = (isEqualExpression as? KtCallExpression)?.valueArgumentList?.text

      pending.add(expression.text to "assertThat($collectionExpr).containsOnly$isEqualArgs")
    }
  }

  private fun KtExpression.callsSingleWithNoArgumentAtTheEnd(): Boolean {
    // If argument expression does actually end with single(), then this would mean that the whole expression is
    // a dot qualified expression with single as its selector expression
    val selectorExpression = (this as? KtDotQualifiedExpression)?.selectorExpression ?: return false

    // Check if the callee is single() with no arguments e.g. single { it > 0 }
    val referenceExpression = selectorExpression.referenceExpression()?.text
    val valueArguments = (selectorExpression as? KtCallExpression)?.valueArguments
    return referenceExpression == "single" && valueArguments?.isEmpty() == true
  }
}
