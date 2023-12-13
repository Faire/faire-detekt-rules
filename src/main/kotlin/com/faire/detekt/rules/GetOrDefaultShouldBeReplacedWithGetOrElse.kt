package com.faire.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt.Companion.FIVE_MINS
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity.Warning
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.resolve.calls.util.getCalleeExpressionIfAny

/**
 * When using map.getOrDefault(key, defaultValue), it always executes the expression to evaluate the default value
 * and then it's thrown away most of the time.  Additionally, even when passing primitive values, those get autoboxed
 * so this causes unnecessary pressure on the garbage collector since these are ignored most of the time.
 *
 * The getOrElse(key) { defaultValue } extension function is an inlined function which executes the lambda to generate
 * the default value only when it's actually needed resulting in zero memory overhead.
 *
 * Good: `map.getOrElse(key) { defaultValue }`
 * Bad: `map.getOrDefault(key, defaultValue)`
 */
internal class GetOrDefaultShouldBeReplacedWithGetOrElse(config: Config = Config.empty) : Rule(config) {
  override val defaultRuleIdAliases = setOf("USE_GET_OR_ELSE_INSTEAD_OF_GET_OR_DEFAULT")

  override val issue: Issue = Issue(
      id = javaClass.simpleName,
      severity = Warning,
      description = "replace map.getOrDefault(key, defaultValue) with map.getOrElse(key) { defaultValue }",
      debt = FIVE_MINS,
  )

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    // Don't check extension function usages
    if (expression.receiverExpression !is KtReferenceExpression) return

    // TODO(Dan): Enhance this to also check the type of the receiver once we figure out how to do that
    // Note: expression.receiverExpression.references is supposed to provide an array of references but it's empty

    val callee = expression.getCalleeExpressionIfAny() ?: return

    if (callee.text == "getOrDefault" && expression.containsTwoArguments()) {
      report(
          CodeSmell(
              issue = issue,
              entity = Entity.from(expression),
              message = issue.description,
          ),
      )
    }
  }
}

private fun KtDotQualifiedExpression.containsTwoArguments(): Boolean {
  return (lastChild as? KtCallExpression)?.valueArguments?.size == 2
}
