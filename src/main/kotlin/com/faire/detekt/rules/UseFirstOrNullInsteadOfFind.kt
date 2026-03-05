package com.faire.detekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.astReplace
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

private val BANNED_CLASS_IDS = listOf(
    ClassId.topLevel(FqName("kotlin.String")),
    ClassId.topLevel(FqName("kotlin.collections.Iterable")),
)

/**
 * Instead of using find(), use firstOrNull() to retrieve
 * the first element in a collection that satisfies a certain condition
 *
 * Reasons:
 *  Readability: firstOrNull() clearly expresses the intent to find the first element
 *  that matches the specified condition or return null if no such element exists.
 *  find() has a similar behaviour, but isn't explicit about returning null when an
 *  element is not found.
 *
 *  Performance: firstOrNull() stops searching as soon as it finds the first matching
 *  element and returns it. This can be more efficient in scenarios where you only
 *  need to find the first occurrence that satisfies the condition and don't need to
 *  process the entire collection.
 */

internal class UseFirstOrNullInsteadOfFind(config: Config = Config.empty) : Rule(config, "Use firstOrNull() instead of find()"), RequiresAnalysisApi {
  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return
    val receiverExpression = expression.receiverExpression

    if (selectorExpression.referenceExpression()?.text != "find") return
    val findExpression = selectorExpression as? KtCallExpression ?: return

    analyze(expression) {
      val receiverType = receiverExpression.expressionType ?: return@analyze
      if (BANNED_CLASS_IDS.none { receiverType.isSubtypeOf(it) }) return@analyze

      report(
          Finding(
              entity = Entity.from(expression),
              message = description,
          ),
      )
    }

    if (autoCorrect) {
      val arguments = if ((findExpression).lambdaArguments.isNotEmpty()) {
        " ${findExpression.lambdaArguments.joinToString { it.text }}"
      } else {
        "(${findExpression.valueArguments.joinToString { it.text }})"
      }

      findExpression.astReplace(KtPsiFactory(findExpression).createExpression("firstOrNull$arguments"))
    }
  }
}
