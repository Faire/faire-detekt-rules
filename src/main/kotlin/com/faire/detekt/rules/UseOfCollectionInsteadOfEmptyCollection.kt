package com.faire.detekt.rules

import dev.detekt.api.Finding
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.astReplace
import org.jetbrains.kotlin.resolve.calls.util.getCalleeExpressionIfAny

/**
 * Instead of using `emptyMap()`, `emptyList()` or `emptySet()` we should use `mapOf()`, `listOf()`, or `setOf()`.
 *
 * This code convention exists for the purpose of consistency.
 */
internal class UseOfCollectionInsteadOfEmptyCollection(config: Config = Config.empty) : Rule(config, ISSUE) {
  override fun visitCallExpression(expression: KtCallExpression) {
    super.visitCallExpression(expression)

    val callee = expression.getCalleeExpressionIfAny() ?: return

    if (callee.text in setOf("emptyList", "emptySet", "emptyMap")) {
      report(
          Finding(
              entity = Entity.from(expression),
              message = description,
          ),
      )

      if (autoCorrect) {
        when (callee.text) {
          "emptyList" -> callee.astReplace(KtPsiFactory(callee).createExpression("listOf"))
          "emptySet" -> callee.astReplace(KtPsiFactory(callee).createExpression("setOf"))
          "emptyMap" -> callee.astReplace(KtPsiFactory(callee).createExpression("mapOf"))
        }
      }
    }
  }

  companion object {
    const val ISSUE = "replace emptySet(), emptyList(), or emptyMap() with setOf(), listOf(), or mapOf() respectively"
  }
}
