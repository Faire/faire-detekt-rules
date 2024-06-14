package com.faire.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
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
internal class WithReceiverShouldHaveMultipleActions(config: Config = Config.empty) : Rule(config) {
    override val issue: Issue = Issue(
        id = javaClass.simpleName,
        severity = Severity.Style,
        description = "With block receiver should have multiple actions",
        debt = Debt.FIVE_MINS,
    )

    override fun visitBlockExpression(expression: KtBlockExpression) {
        super.visitBlockExpression(expression)
        val parentWithStatement = expression.getParentOfType<KtCallExpression>(true) ?: return
        if (!parentWithStatement.isWithExpr()) return

        if (expression.countChildren(null) <= 1) {
            report(
                CodeSmell(
                    issue = issue,
                    entity = Entity.from(parentWithStatement),
                    message = issue.description,
                ),
            )
        }
    }
}

private fun KtCallExpression.isWithExpr(): Boolean = calleeExpression?.textMatches("with") == true
