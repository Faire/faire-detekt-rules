package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRule
import com.faire.detekt.utils.isAssertThat
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
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
internal class DoNotUseHasSizeForEmptyListInAssert(config: Config = Config.empty) :
    AutoCorrectRule(config, "Do not call hasSize(0) on an empty collection, call isEmpty().") {

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return

    val hasSizeExpression = selectorExpression as? KtCallExpression ?: return

    if (!expression.receiverExpression.isAssertThat()) return

    val callee = expression.getCalleeExpressionIfAny() ?: return

    if (callee.text == "hasSize" && expression.isZero()) {
      report(
          Finding(
              entity = Entity.from(expression),
              message = description,
          ),
      )

      if (autoCorrect) {
        pending.add(hasSizeExpression.text to "isEmpty()")
      }
    }
  }
}

private fun KtDotQualifiedExpression.isZero(): Boolean {
  val size = (lastChild as? KtCallExpression)?.getValueArgumentsInParentheses()?.first()?.asElement()?.firstChild

  return size?.text == "0"
}
