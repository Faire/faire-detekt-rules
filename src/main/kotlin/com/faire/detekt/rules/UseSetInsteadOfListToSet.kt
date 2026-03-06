package com.faire.detekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.astReplace

/**
 * Use `set()` instead of `list().toSet()`
 *
 * ```
 * // Good
 * session.createCriteria<T>.addEq("foo", it).set()
 *
 * // Bad
 * session.createCriteria<T>.addEq("foo", it).list().toSet()
 * ```
 */
internal class UseSetInsteadOfListToSet(config: Config = Config.empty,) :
    Rule(config, "Use set() instead of list().toSet()") {
  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return
    val receiverExpression = expression.receiverExpression

    if (selectorExpression.text != "toSet()") return

    if (receiverExpression.lastChild.text != "list()") return

    report(
        Finding(
            entity = Entity.from(expression),
            message = description,
        ),
    )

    if (autoCorrect) {
      expression.lastChild.delete() // Delete "toSet()"
      expression.lastChild.delete() // Delete "."

      val expressionToReplace = receiverExpression.lastChild // "list()"

      expressionToReplace.astReplace(KtPsiFactory(expressionToReplace).createExpression("set()"))
    }
  }
}
