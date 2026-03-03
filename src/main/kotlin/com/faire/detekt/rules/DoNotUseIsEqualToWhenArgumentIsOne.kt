package com.faire.detekt.rules

import com.faire.detekt.utils.isAssertThat
import com.faire.detekt.utils.usesSizeProperty
import dev.detekt.api.Finding
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Rule
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
internal class DoNotUseIsEqualToWhenArgumentIsOne(config: Config = Config.empty) : Rule(config, "Do not use isEqualTo(1), use isOne() instead.") {
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
          Finding(
              entity = Entity.from(expression),
              message = description,
          ),
      )

      if (autoCorrect) {
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
