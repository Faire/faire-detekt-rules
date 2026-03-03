package com.faire.detekt.rules

import com.faire.detekt.utils.isTypeResolutionAvailable
import dev.detekt.api.Finding
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Rule
import dev.detekt.api.RequiresAnalysisApi
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForReceiver
import org.jetbrains.kotlin.resolve.calls.util.getType
import org.jetbrains.kotlin.types.typeUtil.isEnum

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
internal class UseEntriesInsteadOfValuesOnEnum(config: Config = Config.empty) : Rule(config, "Do not call .values() on an Enum. Use .entries instead"), RequiresAnalysisApi {
  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    if (!isTypeResolutionAvailable()) {
      return
    }

    val selectorExpression = expression.selectorExpression ?: return
    if (selectorExpression.text != "values()") return

//    val receiverType = expression.getQualifiedExpressionForReceiver()?.getType(bindingContext)
//        ?: return
//
//    if (!receiverType.isEnum()) return
//
//    report(
//        Finding(
//            entity = Entity.from(expression),
//            message = description,
//        ),
//    )
  }
}
