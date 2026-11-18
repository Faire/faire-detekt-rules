package com.faire.detekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtAnnotatedExpression
import org.jetbrains.kotlin.psi.KtAnonymousInitializer
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCatchClause
import org.jetbrains.kotlin.psi.KtContainerNodeForControlStructureBody
import org.jetbrains.kotlin.psi.KtDeclarationWithBody
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFinallySection
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtLabeledExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtTryExpression
import org.jetbrains.kotlin.psi.KtWhenEntry

/**
 * This rule checks that the return value of a let is used - assigned, returned or captured in a collection.
 *
 * This is a convention we have chosen in order to keep our test assertions consistent.
 *
 * A let that ends a lambda counts as used, because its value is the lambda's result.
 *
 * Good:
 * if (user != null) {
 *   updateState(user)
 * }
 *
 * Bad: You must use the return value from a let, use an if here instead
 * user?.let { updateState(it) }
 *
 */
internal class ReturnValueOfLetMustBeUsed(config: Config = Config.empty) :
    Rule(config, "Must use return value of let") {
  override fun visitCallExpression(expression: KtCallExpression) {
    super.visitCallExpression(expression)

    if (!expression.isLetExpr() || expression.isValueUsed()) return

    report(
        Finding(
            entity = Entity.from(expression),
            message = description,
        ),
    )
  }

  private fun KtCallExpression.isLetExpr(): Boolean = calleeExpression?.textMatches("let") == true

  private fun KtExpression.isValueUsed(): Boolean = when (val parent = parent) {
    is KtQualifiedExpression -> this == parent.receiverExpression || parent.isValueUsed()

    is KtParenthesizedExpression, is KtLabeledExpression, is KtAnnotatedExpression, is KtTryExpression ->
      parent.isValueUsed()

    is KtBlockExpression -> this == parent.statements.lastOrNull() && parent.isValueUsed()

    is KtFunctionLiteral -> true

    is KtDeclarationWithBody -> this != parent.bodyBlockExpression

    is KtContainerNodeForControlStructureBody -> (parent.parent as? KtIfExpression)?.isValueUsed() ?: false

    is KtWhenEntry, is KtCatchClause -> (parent.parent as? KtExpression)?.isValueUsed() ?: true

    is KtFinallySection, is KtAnonymousInitializer -> false

    else -> true
  }
}
