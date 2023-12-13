package com.faire.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Config.Companion.empty
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtValueArgumentList

internal class DoNotUseDirectReceiverReferenceInsideWith(config: Config = empty) : Rule(config) {
  override val issue: Issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.Warning,
      description = "Do not use a direct receiver reference inside a with block, instead use the properties",
      debt = Debt.FIVE_MINS,
  )

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
    if (receiverList.contains(expression.text) && !expression.isReceiverParam() && !expression.isProperty()) {
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

private fun KtExpression.isReceiverParam(): Boolean {
  val potentialWithExpression = parent.parent.parent
  if (potentialWithExpression !is KtCallExpression) return false

  return potentialWithExpression.isWithExpr() && potentialWithExpression.parent !is KtDotQualifiedExpression
}

private fun KtExpression.isProperty(): Boolean {
  return parent is KtDotQualifiedExpression && parent.children.size == 2 && parent.children[1] == this
}

private fun KtCallExpression.isWithExpr(): Boolean = calleeExpression?.textMatches("with") == true
