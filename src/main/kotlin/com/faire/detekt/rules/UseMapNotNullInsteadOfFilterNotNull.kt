package com.faire.detekt.rules

import com.faire.detekt.utils.simplifyCollectionPatterns
import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
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
internal class UseMapNotNullInsteadOfFilterNotNull(config: Config = Config.empty) : Rule(config) {
  override val issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.Warning,
      description = "use mapNotNull() instead of map followed by filerNotNull()",
      debt = Debt.FIVE_MINS,
  )

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return
    if (selectorExpression.text != "filterNotNull()") return

    val receiverExpression = expression.receiverExpression
    if (!receiverExpression.lastChild.text.matches(MAP_REGEX)) return

    report(
        CodeSmell(
            issue = issue,
            entity = Entity.from(expression),
            message = issue.description,
        ),
    )

    withAutoCorrect {
      removeCallToFilterNotNull(expression)
      receiverExpression.simplifyCollectionPatterns("mapNotNull")
    }
  }

  private fun removeCallToFilterNotNull(expression: KtDotQualifiedExpression) {
    expression.lastChild.delete()
    expression.lastChild.delete()
  }
}
