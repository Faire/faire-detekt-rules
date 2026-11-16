package com.faire.detekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.isAncestor
import org.jetbrains.kotlin.psi.psiUtil.parents

private const val WITH = "with"
private const val KOTLIN_WITH = "kotlin.with"

internal class NestedWithIsBanned(config: Config = Config.empty) : Rule(config, "Nested with calls are banned") {
  override fun visitCallExpression(expression: KtCallExpression) {
    super.visitCallExpression(expression)

    if (!expression.isWithShapedCall()) {
      return
    }
    val withNames = expression.containingKtFile.withCallNames()
    if (expression.calleeName() !in withNames) {
      return
    }

    val isNested = expression.parents
        .filterIsInstance<KtCallExpression>()
        .any { ancestor ->
          ancestor.isWithShapedCall() &&
              ancestor.calleeName() in withNames &&
              ancestor.withBlock()?.isAncestor(expression, strict = true) == true
        }

    if (isNested) {
      report(
          Finding(
              entity = Entity.from(expression),
              message = "Nested with calls are banned. Use a named variable for the inner receiver instead.",
          ),
      )
    }
  }

  private fun KtFile.withCallNames(): Set<String> {
    val aliases = importList?.imports.orEmpty()
        .filter { import -> import.importedFqName?.asString() == KOTLIN_WITH }
        .mapNotNull { import -> import.aliasName }
    return setOf(WITH) + aliases
  }

  private fun KtCallExpression.calleeName(): String? =
      (calleeExpression as? KtNameReferenceExpression)?.getReferencedName()

  private fun KtCallExpression.isWithShapedCall(): Boolean =
      calleeName() != null && !hasReceiver() && valueArguments.size == 2 && withBlock() != null

  private fun KtCallExpression.hasReceiver(): Boolean = (parent as? KtQualifiedExpression)?.selectorExpression == this

  private fun KtCallExpression.withBlock(): KtLambdaExpression? {
    val lastArgument = valueArguments.lastOrNull()?.getArgumentExpression() ?: return null
    return KtPsiUtil.safeDeparenthesize(lastArgument) as? KtLambdaExpression
  }
}
