package com.faire.detekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.resolve.calls.util.getCalleeExpressionIfAny

/**
 * When using map.getOrDefault(key, defaultValue), it always executes the expression to evaluate the default value
 * and then it's thrown away some of the time.  Additionally, even when passing primitive values, those get autoboxed
 * so this causes unnecessary pressure on the garbage collector since these are ignored some of the time.
 *
 * The getOrElse(key) { defaultValue } extension function is an inlined function which executes the lambda to generate
 * the default value only when it's actually needed resulting in zero memory overhead.
 *
 * Good: `map.getOrElse(key) { defaultValue }`
 * Bad: `map.getOrDefault(key, defaultValue)`
 */
internal class GetOrDefaultShouldBeReplacedWithGetOrElse(config: Config = Config.empty,) :
    Rule(config, "replace map.getOrDefault(key, defaultValue) with map.getOrElse(key) { defaultValue }") {
//  override val defaultRuleIdAliases = setOf("USE_GET_OR_ELSE_INSTEAD_OF_GET_OR_DEFAULT")

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    // Don't check extension function usages
    if (expression.receiverExpression !is KtReferenceExpression) return

    // TODO(Dan): Enhance this to also check the type of the receiver once we figure out how to do that
    // Note: expression.receiverExpression.references is supposed to provide an array of references but it's empty

    val callee = expression.getCalleeExpressionIfAny() ?: return

    if (callee.text == "getOrDefault" && expression.containsTwoArguments()) {
      report(
          Finding(
              entity = Entity.from(expression),
              message = description,
          ),
      )
    }
  }
}

private fun KtDotQualifiedExpression.containsTwoArguments(): Boolean =
    (lastChild as? KtCallExpression)?.valueArguments?.size == 2
