package com.faire.detekt.rules

import com.faire.detekt.utils.isAssertThat
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

/**
 * Use `isEqualTo(0)` instead of `isZero()` when asserting on a Number.
 *
 * ```
 * @Test
 * fun `check that there were no failures`() {
 *   // Bad
 *   assertThat(failuresCount).isZero()
 *
 *   // Good
 *   assertThat(failuresCount).isEqualTo(0)
 * }
 * ```
 */
internal class DoNotUseIsZeroAssertions(config: Config = Config.empty) : Rule(config, description = "Do not use isZero(), use isEqualTo(0) instead.") {
  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return
    val receiverExpression = expression.receiverExpression

    if (!receiverExpression.isAssertThat()) return

    if (selectorExpression.referenceExpression()?.text == "isZero") {
      report(
          Finding(entity = Entity.from(expression), message = "Do not use isZero(), use isEqualTo(0) instead."),
      )
    }
  }
}
