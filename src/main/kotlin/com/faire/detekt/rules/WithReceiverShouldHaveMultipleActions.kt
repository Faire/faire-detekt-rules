package com.faire.detekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

/**
 * In a with() block there must be multiple statements acting on the receiver.
 *
 * Good example:
 *
 * ```
 * fun `test usage`() {
 *    with(subject.action()) {
 *      assertThat(property).isEqualTo(true)
 *      assertThat(otherProperty).isEqualTo(false)
 *    }
 * ```
 *
 * Bad example:
 *
 * ```
 * fun `test usage`() {
 *    with(subject.action()) {
 *      assertThat(property).isEqualTo(true)
 *    }
 * ```
 */
internal class WithReceiverShouldHaveMultipleActions(config: Config = Config.empty,) :
    Rule(config, "With block receiver should have multiple actions") {
  override fun visitBlockExpression(expression: KtBlockExpression) {
    super.visitBlockExpression(expression)
    val parentWithStatement = expression.getParentOfType<KtCallExpression>(true) ?: return
    if (!parentWithStatement.isWithExpr()) return

    if (expression.countChildren(null) <= 1) {
      report(
          Finding(
              entity = Entity.from(parentWithStatement),
              message = description,
          ),
      )
    }
  }
}

private fun KtCallExpression.isWithExpr(): Boolean = calleeExpression?.textMatches("with") == true
