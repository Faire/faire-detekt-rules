package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRule
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtValueArgumentList

private val FILTER_REGEX = ".*\\.*filter\\s*(\\{|\\().+".toRegex()

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

    val receiverExpression = expression.receiverExpression
    val finalMethodInChain = receiverExpression.lastChild
    // an argument-less method invocation is not an invocation of Iterable<T>::filter
    if ((finalMethodInChain.lastChild as? KtValueArgumentList)?.arguments?.isEmpty() == true) return
    // if there is more than one KtLambdaArgument, it is not a call to Iterable<T>::filter
    if (finalMethodInChain.children.count { it is KtLambdaArgument } > 1) return
    if (!receiverExpression.lastChild.text.matches(FILTER_REGEX)) return

    report(
        Finding(
            entity = Entity.from(expression),
            message = description,
        ),
    )

    if (autoCorrect) {
      val receiverDotExpr = receiverExpression as? KtDotQualifiedExpression ?: return
      val filterCall = receiverDotExpr.selectorExpression as? KtCallExpression ?: return
      val filterCallText = filterCall.text
      val newCallText = filterCallText.replaceFirst("filter", "single")
      val baseReceiverText = receiverDotExpr.receiverExpression.text
      // Extract whitespace/dot between base receiver and filter call from the expression text
      val betweenBaseAndFilter = receiverDotExpr.text.removePrefix(baseReceiverText).removeSuffix(filterCallText)

      pending.add(expression.text to "$baseReceiverText$betweenBaseAndFilter$newCallText")
    }
  }
}

private fun buildArgumentsText(call: KtCallExpression): String = if (call.lambdaArguments.isNotEmpty()) {
    " ${call.lambdaArguments.joinToString { it.text }}"
  } else {
    "(${call.valueArguments.joinToString { it.text }})"
  }
