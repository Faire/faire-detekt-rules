package com.faire.detekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Config.Companion.empty
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.psi.KtValueArgumentName
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelector

internal class DoNotUseDirectReceiverReferenceInsideWith(config: Config = empty) :
    Rule(config, "Do not use a direct receiver reference inside a with block, instead use the properties") {
  private val receiverList = mutableListOf<String>()

  override fun visitCallExpression(expression: KtCallExpression) {
    val receiver = expression.withReceiverOrNull()
    if (receiver != null) receiverList.add(receiver)

    super.visitCallExpression(expression)

    if (receiver != null) receiverList.remove(receiver)
  }

  override fun visitExpression(expression: KtExpression) {
    super.visitExpression(expression)
    if (receiverList.isEmpty()) return

    if (
        receiverList.contains(expression.text)
        && !expression.isReceiverParam()
        && !expression.isMemberOrTypeName()
        && !expression.isNamedArgument()
    ) {
      report(
          Finding(
              entity = Entity.from(expression),
              message = description,
          ),
      )
    }
  }
}

private fun KtExpression.isReceiverParam(): Boolean {
  val potentialWithExpression = parent.parent.parent
  if (potentialWithExpression !is KtCallExpression) return false

  return potentialWithExpression.isWithExpr()
}

private fun KtExpression.isMemberOrTypeName(): Boolean = when (val parent = parent) {
  is KtQualifiedExpression -> parent.selectorExpression == this
  is KtCallExpression -> parent.calleeExpression == this
  is KtCallableReferenceExpression -> parent.callableReference == this
  is KtUserType -> true
  else -> false
}

private fun KtCallExpression.withReceiverOrNull(): String? =
    if (isWithExpr()) valueArgumentList?.arguments?.firstOrNull()?.getArgumentExpression()?.text else null

// Calls like `scope.with(...)` are extension functions named with, not the standard library's with.
private fun KtCallExpression.isWithExpr(): Boolean =
    calleeExpression?.textMatches("with") == true && getQualifiedExpressionForSelector() == null

private fun KtExpression.isNamedArgument(): Boolean = this is KtNameReferenceExpression && parent is KtValueArgumentName
