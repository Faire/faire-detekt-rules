package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRule
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

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
internal class UseSetInsteadOfListToSet(config: Config = Config.empty) :
    AutoCorrectRule(config, "Use set() instead of list().toSet()") {

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
      // Replace .list().toSet() with .set()
      val receiverDotExpr = receiverExpression as? KtDotQualifiedExpression ?: return
      val listCallText = receiverDotExpr.selectorExpression?.text ?: return // "list()"
      val baseReceiverText = receiverDotExpr.receiverExpression.text
      // Extract whitespace/dot between base receiver and list() from the expression text
      val betweenBaseAndList = receiverDotExpr.text.removePrefix(baseReceiverText).removeSuffix(listCallText)

      pending.add(expression.text to "$baseReceiverText${betweenBaseAndList}set()")
    }
  }
}
