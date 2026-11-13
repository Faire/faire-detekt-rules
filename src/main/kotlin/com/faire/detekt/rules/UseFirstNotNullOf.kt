package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRule
import com.faire.detekt.utils.hasReturnLabelled
import com.faire.detekt.utils.singleArgumentCallOrNull
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

/**
 * Instead of using `.mapNotNull { ... }.first()`, use `firstNotNullOf { ... }` to transform and select
 * an element in a collection simultaneously.
 *
 * Reasons:
 *  Performance: `firstNotNullOf {}` processes each element at most once and early returns if it finds a not-null
 *               result after the transformation. On the other hand, if not used on a sequence, `.mapNotNull {}.first()`
 *               processes the entire collection, which performs transformations on elements we immediately throw away.
 *
 *  Readability: `firstNotNullOf {}` clearly expresses the intent to select the first element after the transformation.
 *               Using `.mapNotNull { ... }.first()` is more verbose.
 */
internal class UseFirstNotNullOf(config: Config = Config.empty) :
    AutoCorrectRule(config, "use firstNotNullOf() instead of mapNotNull followed by first()") {

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return
    if (selectorExpression.text != "first()") return

    val receiverExpression = expression.receiverExpression as? KtDotQualifiedExpression ?: return
    val mapNotNullCall = receiverExpression.singleArgumentCallOrNull("mapNotNull") ?: return

    report(
        Finding(
            entity = Entity.from(expression),
            message = description,
        ),
    )

    if (autoCorrect && !mapNotNullCall.hasReturnLabelled("mapNotNull")) {
      val mapNotNullCallText = mapNotNullCall.text
      val newCallText = mapNotNullCallText.replaceFirst("mapNotNull", "firstNotNullOf")

      val mapNotNullReceiver = receiverExpression.receiverExpression
      val asSeqExpr = (mapNotNullReceiver as? KtDotQualifiedExpression)
          ?.takeIf { it.selectorExpression?.text == "asSequence()" }

      if (asSeqExpr != null) {
        // Strip .asSequence() — use the receiver before asSequence() as base
        val baseReceiverText = asSeqExpr.receiverExpression.text
        val asSeqCallText = asSeqExpr.selectorExpression?.text ?: return
        // Get whitespace/dot between base receiver and asSequence call
        val betweenBaseAndAsSeq = asSeqExpr.text.removePrefix(baseReceiverText).removeSuffix(asSeqCallText)
        replace(expression, "$baseReceiverText$betweenBaseAndAsSeq$newCallText")
      } else {
        val baseReceiverText = mapNotNullReceiver.text
        val betweenBaseAndMapNotNull =
          receiverExpression.text.removePrefix(baseReceiverText).removeSuffix(mapNotNullCallText)
        replace(expression, "$baseReceiverText$betweenBaseAndMapNotNull$newCallText")
      }
    }
  }
}
