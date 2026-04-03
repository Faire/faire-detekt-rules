package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRule
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

private val MAP_REGEX = ".*\\.*map\\s*(\\{|\\().+".toRegex()

/**
 * Instead of using .map {}.filterNotNull(), use mapNotNull {} to transform and filter
 * elements in a collection simultaneously.
 *
 * Reasons:
 *  Readability: mapNotNull {} clearly expresses the intent to transform each element
 *  and simultaneously filter out the null results. Using .map {}.filterNotNull() in
 *  sequence can be more verbose and might not immediately convey the intent of
 *  filtering out nulls.
 *
 *  Performance: mapNotNull {} processes each element only once, applying the
 *  transformation and filtering out nulls in a single pass. On the other hand,
 *  using .map {}.filterNotNull() processes the collection twice: once for the
 *  transformation and once for filtering, which can be less efficient especially
 *  for larger collections.
 */
internal class UseMapNotNullInsteadOfFilterNotNull(config: Config = Config.empty) :
    AutoCorrectRule(config, "use mapNotNull() instead of map followed by filerNotNull()") {

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return
    if (selectorExpression.text != "filterNotNull()") return

    val receiverExpression = expression.receiverExpression
    if (!receiverExpression.lastChild.text.matches(MAP_REGEX)) return

    report(
        Finding(
            entity = Entity.from(expression),
            message = description,
        ),
    )

    if (autoCorrect) {
      val receiverDotExpr = receiverExpression as? KtDotQualifiedExpression ?: return
      val mapCall = receiverDotExpr.selectorExpression as? KtCallExpression ?: return
      val mapCallText = mapCall.text
      val newCallText = mapCallText.replaceFirst("map", "mapNotNull")
      val baseReceiverText = receiverDotExpr.receiverExpression.text
      val betweenBaseAndMap = receiverDotExpr.text.removePrefix(baseReceiverText).removeSuffix(mapCallText)

      pending.add(expression.text to "$baseReceiverText$betweenBaseAndMap$newCallText")
    }
  }
}

private fun buildArgumentsText(call: KtCallExpression): String = if (call.lambdaArguments.isNotEmpty()) {
    " ${call.lambdaArguments.joinToString { it.text }}"
  } else {
    "(${call.valueArguments.joinToString { it.text }})"
  }
