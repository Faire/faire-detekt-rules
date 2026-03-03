package com.faire.detekt.rules

import com.faire.detekt.utils.simplifyCollectionPatterns
import dev.detekt.api.Finding
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

private val MAP_REGEX = ".*\\.*map\\s*(\\{|\\().+".toRegex()

/**
 * Instead of using .map {}.filterNotNull(), use mapNotNull {} to transform and filter
 * elements in a collection simultaneously.
 *
 * Reasons:
 *  Readability: mapNotNull {} clearly expresses the intent to transform each element
 *  and simultaneously filter out the null results. Using .map {}.filterNotNull() in
 *  sequence can be more verbose and might not immediately convey the intent of
 *  filtering out nulls.
 *
 *  Performance: mapNotNull {} processes each element only once, applying the
 *  transformation and filtering out nulls in a single pass. On the other hand,
 *  using .map {}.filterNotNull() processes the collection twice: once for the
 *  transformation and once for filtering, which can be less efficient especially
 *  for larger collections.
 */
internal class UseMapNotNullInsteadOfFilterNotNull(config: Config = Config.empty) : Rule(config, "use mapNotNull() instead of map followed by filerNotNull()") {
  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return
    if (selectorExpression.text != "filterNotNull()") return

    val receiverExpression = expression.receiverExpression
    if (!receiverExpression.lastChild.text.matches(MAP_REGEX)) return

    report(
        Finding(
            entity = Entity.from(expression),
            message = description,
        ),
    )

    if (autoCorrect) {
      removeCallToFilterNotNull(expression)
      receiverExpression.simplifyCollectionPatterns("mapNotNull")
    }
  }

  private fun removeCallToFilterNotNull(expression: KtDotQualifiedExpression) {
    expression.lastChild.delete()
    expression.lastChild.delete()
  }
}
