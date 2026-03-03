package com.faire.detekt.rules

import dev.detekt.api.Finding
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.resolve.calls.util.getCalleeExpressionIfAny

/**
 * Detects usage of `mapNotNull { it }` and suggests using `filterNotNull()` instead.
 *
 * Noncompliant code:
 * ```kotlin
 * listOf(1, null, 2).mapNotNull { it }
 * ```
 *
 * Compliant code:
 * ```kotlin
 * listOf(1, null, 2).filterNotNull()
 * ```
 */
internal class FilterNotNullOverMapNotNullForFiltering(config: Config = Config.empty) : Rule(config, "Use filterNotNull() instead of mapNotNull { it }") {
  override fun visitCallExpression(expression: KtCallExpression) {
    super.visitCallExpression(expression)

    val callee = expression.getCalleeExpressionIfAny() ?: return
    if (callee.text != "mapNotNull") return

    val lambda = expression.lambdaArguments.firstOrNull()?.getLambdaExpression() ?: return
    val lambdaBody = lambda.bodyExpression?.children?.firstOrNull() ?: return

    if (lambdaBody is KtNameReferenceExpression && lambdaBody.getReferencedName() == "it") {
      report(
          Finding(
              entity = Entity.from(expression),
              message = "Replace mapNotNull { it } with filterNotNull()",
          ),
      )

      if (autoCorrect) {
        val dotQualified = expression.parent as? KtDotQualifiedExpression ?: return
        val receiver = dotQualified.receiverExpression.text
        val psiFactory = KtPsiFactory(expression)
        val newExpression = psiFactory.createExpression("$receiver.filterNotNull()")
        dotQualified.replace(newExpression)
      }
    }
  }
}
