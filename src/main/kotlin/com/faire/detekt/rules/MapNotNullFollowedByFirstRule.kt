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
      // The replacement already stops at the first non-null result, so an asSequence() right before mapNotNull is
      // redundant.
      val asSequenceCall = (receiverExpression.receiverExpression as? KtDotQualifiedExpression)
          ?.selectorExpression
          ?.takeIf { it.text == "asSequence()" }
      val firstReplacedCall = asSequenceCall ?: mapNotNullCall
      val keptLength = firstReplacedCall.textRange.startOffset - expression.textRange.startOffset
      val newCallText = mapNotNullCall.text.replaceFirst("mapNotNull", replacementFunctionName)
      pending.add(expression.text to expression.text.take(keptLength) + newCallText)
    }
  }
}
