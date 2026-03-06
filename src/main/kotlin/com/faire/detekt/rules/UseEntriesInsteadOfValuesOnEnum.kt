package com.faire.detekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

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
internal class UseEntriesInsteadOfValuesOnEnum(config: Config = Config.empty,) :
    Rule(config, "Do not call .values() on an Enum. Use .entries instead") {
  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return
    if (selectorExpression.text != "values()") return

    report(
        Finding(
            entity = Entity.from(expression),
            message = description,
        ),
    )
  }
}
