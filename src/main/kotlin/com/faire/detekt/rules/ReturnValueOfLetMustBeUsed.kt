package com.faire.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.rules.LET_LITERAL
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDestructuringDeclaration
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtSafeQualifiedExpression
import org.jetbrains.kotlin.psi.KtValueArgument

/**
 * This rule checks that the return value of a let is used - assigned, returned or captured in a collection.
 *
 * This is a convention we have chosen in order to keep our test assertions consistent.
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
internal class ReturnValueOfLetMustBeUsed(config: Config = Config.empty) : Rule(config) {
  override val issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.Style,
      description = "Must use return value of let",
      debt = Debt.FIVE_MINS,
  )

  override fun visitCallExpression(expression: KtCallExpression) {
    super.visitCallExpression(expression)

    if (!expression.isLetExpr()) return

    var currentParent = expression.parent
    var currentChild: PsiElement = expression
    while (currentParent != null) {
      when {
        currentParent is KtReturnExpression -> return
        currentParent is KtProperty -> return
        currentParent is KtBinaryExpression -> return
        // that the let call is not last in a chain of dot qualified expressions
        currentParent is KtDotQualifiedExpression && currentChild == currentParent.receiverExpression -> return
        currentParent is KtSafeQualifiedExpression && currentChild == currentParent.receiverExpression -> return
        currentParent is KtValueArgument -> return
        currentParent is KtNamedFunction && isSingleLineFunction(currentParent) -> return
        currentParent is KtDestructuringDeclaration -> return

        else -> {
          currentChild = currentChild.parent
          currentParent = currentParent.parent
        }
      }
    }

    report(
        CodeSmell(
            issue = issue,
            entity = Entity.from(expression),
            message = issue.description,
        ),
    )
  }

  private fun isSingleLineFunction(expression: KtNamedFunction): Boolean {
    var child = expression.firstChild
    while (child != null) {
      if (child.textMatches("=")) return true
      child = child.nextSibling
    }
    return false
  }
}

private fun KtCallExpression.isLetExpr(): Boolean = calleeExpression?.textMatches(LET_LITERAL) == true
