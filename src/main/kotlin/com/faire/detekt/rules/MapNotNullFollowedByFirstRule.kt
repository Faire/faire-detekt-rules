package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRule
import com.faire.detekt.utils.hasReturnLabelled
import com.faire.detekt.utils.singleArgumentCallOrNull
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

internal abstract class MapNotNullFollowedByFirstRule(
    config: Config,
    private val firstFunctionName: String,
    private val replacementFunctionName: String,
) : AutoCorrectRule(
    config,
    "use $replacementFunctionName() instead of mapNotNull followed by $firstFunctionName()",
) {

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return
    if (selectorExpression.text != "$firstFunctionName()") return

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
      val newCallText = mapNotNullCallText.replaceFirst("mapNotNull", replacementFunctionName)

      val mapNotNullReceiver = receiverExpression.receiverExpression
      val asSeqExpr = (mapNotNullReceiver as? KtDotQualifiedExpression)
          ?.takeIf { it.selectorExpression?.text == "asSequence()" }

      if (asSeqExpr != null) {
        // Strip .asSequence() — use the receiver before asSequence() as base
        val baseReceiverText = asSeqExpr.receiverExpression.text
        val asSeqCallText = asSeqExpr.selectorExpression?.text ?: return
        // Get whitespace/dot between base receiver and asSequence call
        val betweenBaseAndAsSeq = asSeqExpr.text.removePrefix(baseReceiverText).removeSuffix(asSeqCallText)
        pending.add(expression.text to "$baseReceiverText$betweenBaseAndAsSeq$newCallText")
      } else {
        val baseReceiverText = mapNotNullReceiver.text
        val betweenBaseAndMapNotNull =
          receiverExpression.text.removePrefix(baseReceiverText).removeSuffix(mapNotNullCallText)
        pending.add(expression.text to "$baseReceiverText$betweenBaseAndMapNotNull$newCallText")
      }
    }
  }
}
