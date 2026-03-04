package com.faire.detekt.rules

import com.intellij.psi.PsiElement
import dev.detekt.api.Finding
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDestructuringDeclaration
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtSafeQualifiedExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes

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
internal class ReturnValueOfLetMustBeUsed(config: Config = Config.empty) : Rule(config, "Must use return value of let") {
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
        currentParent is KtParameter && currentParent.elementType == KtStubElementTypes.VALUE_PARAMETER -> return

        else -> {
          currentChild = currentChild.parent
          currentParent = currentParent.parent
        }
      }
    }

    report(
        Finding(
            entity = Entity.from(expression),
            message = description,
        ),
    )
  }

  private fun KtCallExpression.isLetExpr(): Boolean = calleeExpression?.textMatches("let") == true

  private fun isSingleLineFunction(expression: KtNamedFunction): Boolean {
    var child = expression.firstChild
    while (child != null) {
      if (child.textMatches("=")) return true
      child = child.nextSibling
    }
    return false
  }
}
