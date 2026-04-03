package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRule
import com.faire.detekt.utils.isAssertThat
import com.faire.detekt.utils.usesSizeProperty
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
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
internal class DoNotUseIsEqualToWhenArgumentIsZero(config: Config = Config.empty) :
    AutoCorrectRule(config, "Do not use isEqualTo(0), use isZero() instead.") {

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
          Finding(
              entity = Entity.from(expression),
              message = description,
          ),
      )

      if (autoCorrect) {
        pending.add(isEqualToExpression.text to "isZero()")
      }
    }
  }
}

private fun KtCallExpression.isComparingToZero(): Boolean {
  val argument = valueArguments.singleOrNull()?.text ?: return false

  return argument in ZERO_TEXTS
}
