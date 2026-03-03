package com.faire.detekt.rules

import dev.detekt.api.Finding
import dev.detekt.api.Config
import dev.detekt.api.Config.Companion.empty
import dev.detekt.api.Entity
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.psi.KtValueArgumentName

internal class DoNotUseDirectReceiverReferenceInsideWith(config: Config = empty) : Rule(config, "Do not use a direct receiver reference inside a with block, instead use the properties") {
  private val receiverList = mutableListOf<String>()

  override fun visitCallExpression(expression: KtCallExpression) {
    var inWithBlock = false
    var receiver = ""
    if (expression.isWithExpr() && expression.parent !is KtDotQualifiedExpression) {
      val argumentsList: KtValueArgumentList = expression.children.first { it is KtValueArgumentList }
          as KtValueArgumentList
      receiver = argumentsList.arguments.first().text
      receiverList.add(receiver)
      inWithBlock = true
    }

    super.visitCallExpression(expression)

    if (inWithBlock) {
      receiverList.remove(receiver)
    }
  }

  override fun visitExpression(expression: KtExpression) {
    super.visitExpression(expression)
    if (receiverList.isEmpty()) return

    if (
        receiverList.contains(expression.text)
        && !expression.isReceiverParam()
        && !expression.isProperty()
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

  return potentialWithExpression.isWithExpr() && potentialWithExpression.parent !is KtDotQualifiedExpression
}

private fun KtExpression.isProperty(): Boolean {
  return parent is KtDotQualifiedExpression && parent.children.size == 2 && parent.children[1] == this
}

private fun KtCallExpression.isWithExpr(): Boolean = calleeExpression?.textMatches("with") == true

private fun KtExpression.isNamedArgument(): Boolean = this is KtNameReferenceExpression && parent is KtValueArgumentName
