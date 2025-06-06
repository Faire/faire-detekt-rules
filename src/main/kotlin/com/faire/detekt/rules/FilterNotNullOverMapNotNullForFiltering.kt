package com.faire.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
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
internal class FilterNotNullOverMapNotNullForFiltering(config: Config = Config.empty) : Rule(config) {
  override val issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.Style,
      description = "Use filterNotNull() instead of mapNotNull { it }",
      debt = Debt.FIVE_MINS,
  )

  override fun visitCallExpression(expression: KtCallExpression) {
    super.visitCallExpression(expression)

    val callee = expression.getCalleeExpressionIfAny() ?: return
    if (callee.text != "mapNotNull") return

    val lambda = expression.lambdaArguments.firstOrNull()?.getLambdaExpression() ?: return
    val lambdaBody = lambda.bodyExpression?.children?.firstOrNull() ?: return

    if (lambdaBody is KtNameReferenceExpression && lambdaBody.getReferencedName() == "it") {
      report(
          CodeSmell(
              issue = issue,
              entity = Entity.from(expression),
              message = "Replace mapNotNull { it } with filterNotNull()",
          ),
      )

      withAutoCorrect {
        val dotQualified = expression.parent as? KtDotQualifiedExpression ?: return@withAutoCorrect
        val receiver = dotQualified.receiverExpression.text
        val psiFactory = KtPsiFactory(expression)
        val newExpression = psiFactory.createExpression("$receiver.filterNotNull()")
        dotQualified.replace(newExpression)
      }
    }
  }
}
