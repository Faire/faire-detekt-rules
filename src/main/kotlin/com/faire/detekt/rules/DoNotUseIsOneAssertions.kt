package com.faire.detekt.rules

import com.faire.detekt.utils.isAssertThat
import dev.detekt.api.Finding
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

/**
 * Use `isEqualTo(1)` instead of `isOne()` when asserting on a Number.
 *
 * ```
 * @Test
 * fun `check that there were no failures`() {
 *   // Bad
 *   assertThat(failuresCount).isOne()
 *
 *   // Good
 *   assertThat(failuresCount).isEqualTo(1)
 * }
 * ```
 */
internal class DoNotUseIsOneAssertions(config: Config = Config.empty) : Rule(config, "Do not use isOne(), use isEqualTo(1) instead.") {
  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return
    val receiverExpression = expression.receiverExpression

    if (!receiverExpression.isAssertThat()) return

    if (selectorExpression.referenceExpression()?.text == "isOne") {
      report(
          Finding(
              entity = Entity.from(expression),
              message = description,
          ),
      )
    }
  }
}
