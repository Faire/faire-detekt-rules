package com.faire.detekt.rules

import com.faire.detekt.utils.isTypeResolutionAvailable
import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution
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
@RequiresTypeResolution
internal class UseEntriesInsteadOfValuesOnEnum(config: Config = Config.empty) : Rule(config) {
  override val issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.Warning,
      description = "Do not call .values() on an Enum. Use .entries instead",
      debt = Debt.FIVE_MINS,
  )

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    if (!isTypeResolutionAvailable()) {
      return
    }

    val selectorExpression = expression.selectorExpression ?: return
    if (selectorExpression.text != "values()") return

    val receiverType = expression.getQualifiedExpressionForReceiver()?.getType(bindingContext)
        ?: return

    if (!receiverType.isEnum()) return

    report(
        CodeSmell(
            issue = issue,
            entity = Entity.from(expression),
            message = issue.description,
        ),
    )
  }
}
