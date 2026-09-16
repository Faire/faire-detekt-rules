package com.faire.detekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

/**
 * entries was added as a stable replacement for .values() in Kotlin 1.9.0 and is recommended to be used instead.
 * This rule warns when .values() is used on an Enum.
 *
 * Good:
 * for (color in Color.entries)
 *
 * Bad:
 * for (color in Color.values())
 */
internal class UseEntriesInsteadOfValuesOnEnum(config: Config = Config.empty) :
    Rule(config, "Do not call .values() on an Enum. Use .entries instead"),
    RequiresAnalysisApi {
  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val callExpression = expression.selectorExpression as? KtCallExpression ?: return
    if (callExpression.referenceExpression()?.text != "values") return
    if (callExpression.valueArguments.isNotEmpty()) return

    analyze(expression) {
      val functionSymbol = callExpression.resolveToCall()
          ?.successfulFunctionCallOrNull()
          ?.symbol
          ?: return@analyze
      if (functionSymbol.valueParameters.isNotEmpty()) return@analyze

      // the synthetic values() function is declared on the enum class itself
      val containingClass = functionSymbol.containingDeclaration as? KaClassSymbol ?: return@analyze
      if (containingClass.classKind != KaClassKind.ENUM_CLASS) return@analyze

      report(
          Finding(
              entity = Entity.from(expression),
              message = description,
          ),
      )
    }
  }
}
