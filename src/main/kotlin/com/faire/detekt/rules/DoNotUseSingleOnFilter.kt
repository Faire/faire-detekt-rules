package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRule
import com.faire.detekt.utils.singleArgumentCallOrNull
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

/**
 * Use `single { xyz }` instead of `filter { xyz }.single()`.
 *
 * ```
 * // Good
 * invoice.items.single { it.type == TAX_ITEM }
 *
 * // Bad
 * invoice.items.filter { it.type == TAX_ITEM }.single()
 * ```
 *
 * This augments the `UnnecessaryFilter` detekt rule which does not cover `.single` as of 1.21.0.
 */
internal class DoNotUseSingleOnFilter(config: Config = Config.empty) :
    AutoCorrectRule(config, "Do not use single() with filter { ... }, use single { ... } instead") {

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return
    if (selectorExpression.text != "single()") return

    val receiverDotExpr = expression.receiverExpression as? KtDotQualifiedExpression ?: return
    val filterCall = receiverDotExpr.singleArgumentCallOrNull("filter") ?: return

    report(
        Finding(
            entity = Entity.from(expression),
            message = description,
        ),
    )

    if (autoCorrect) {
      val filterCallText = filterCall.text
      val newCallText = filterCallText.replaceFirst("filter", "single")
      val baseReceiverText = receiverDotExpr.receiverExpression.text
      // Extract whitespace/dot between base receiver and filter call from the expression text
      val betweenBaseAndFilter = receiverDotExpr.text.removePrefix(baseReceiverText).removeSuffix(filterCallText)

      replace(expression, "$baseReceiverText$betweenBaseAndFilter$newCallText")
    }
  }
}
