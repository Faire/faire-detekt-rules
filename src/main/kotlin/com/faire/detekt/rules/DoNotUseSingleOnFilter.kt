package com.faire.detekt.rules

import com.faire.detekt.utils.simplifyCollectionPatterns
import dev.detekt.api.Finding
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Rule
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
internal class DoNotUseSingleOnFilter(config: Config = Config.empty) : Rule(config, "Do not use single() with filter { ... }, use single { ... } instead") {
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
      removeCallToSingle(expression)
      receiverExpression.simplifyCollectionPatterns("single")
    }
  }

  private fun removeCallToSingle(expression: KtDotQualifiedExpression) {
    expression.lastChild.delete()
    expression.lastChild.delete()
  }
}
