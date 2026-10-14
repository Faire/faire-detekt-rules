package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRule
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

private val MAP_NOT_NULL_REGEX = """.*\.*mapNotNull\s*[{(].+""".toRegex()

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
    if (!receiverExpression.lastChild.text.matches(MAP_NOT_NULL_REGEX)) return

    report(
        Finding(
            entity = Entity.from(expression),
            message = description,
        ),
    )

    if (autoCorrect) {
      val mapNotNullCall = receiverExpression.selectorExpression as? KtCallExpression ?: return
      val mapNotNullCallText = mapNotNullCall.text
      val newCallText = mapNotNullCallText.replaceFirst("mapNotNull", replacementFunctionName)

      val mapNotNullReceiver = receiverExpression.receiverExpression
      val baseReceiverText = if (
        mapNotNullReceiver is KtDotQualifiedExpression &&
        mapNotNullReceiver.selectorExpression?.text == "asSequence()"
      ) {
        mapNotNullReceiver.receiverExpression.text
      } else {
        mapNotNullReceiver.text
      }

      val betweenReceiverAndMapNotNull =
        receiverExpression.text.removePrefix(mapNotNullReceiver.text).removeSuffix(mapNotNullCallText)
      pending.add(expression.text to "$baseReceiverText$betweenReceiverAndMapNotNull$newCallText")
    }
  }
}
