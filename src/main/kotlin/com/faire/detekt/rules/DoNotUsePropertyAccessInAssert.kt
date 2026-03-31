package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRule
import com.faire.detekt.utils.isAssertThat
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

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

internal class DoNotUsePropertyAccessInAssert(config: Config = Config.empty) :
    AutoCorrectRule(
        config,
        "Do not use property access syntax with assertion methods. Do not remove the parenthesis.",
    ) {

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return
    val receiverExpression = expression.receiverExpression
    if (receiverExpression !is KtCallExpression) return

    if (receiverExpression.isAssertThat() && selectorExpression !is KtCallExpression) {
      report(
          Finding(
              entity = Entity.from(expression),
              message = description,
          ),
      )

      if (autoCorrect) {
        pending.add(selectorExpression.text to "${selectorExpression.text}()")
      }
    }
  }
}
