package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRule
import com.faire.detekt.utils.isAssertThat
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
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
internal class AlwaysUseIsTrueOrIsFalse(config: Config = Config.empty) :
    AutoCorrectRule(config, "Do not use isEqualTo(true) or isEqualTo(false), use isTrue() or isFalse()") {

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return
    val receiverExpression = expression.receiverExpression

    val isEqualToExpression = selectorExpression as? KtCallExpression ?: return

    if (!receiverExpression.isAssertThat()) return

    if (selectorExpression.referenceExpression()?.text != "isEqualTo") return

    if (isEqualToExpression.isComparingToTrue() || isEqualToExpression.isComparingToFalse()) {
      report(
          Finding(
              entity = Entity.from(expression),
              message = "Do not use isEqualTo(true) or isEqualTo(false), use isTrue() or isFalse()",
          ),
      )

      if (autoCorrect) {
        val replacement = if (isEqualToExpression.isComparingToTrue()) "isTrue()" else "isFalse()"
        pending.add(isEqualToExpression.text to replacement)
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
