package com.faire.detekt.rules

import com.faire.detekt.utils.isAssertThat
import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

/**
 * Use `isEqualTo(1)` instead of `isOne()` when asserting on a Number.
 *
 * ```
 * @Test
 * fun `check that there were no failures`() {
 *   // Bad
 *   assertThat(failuresCount).isOne()
 *
 *   // Good
 *   assertThat(failuresCount).isEqualTo(1)
 * }
 * ```
 */
internal class DoNotUseIsOneAssertions(config: Config = Config.empty) : Rule(config) {
    override val issue = Issue(
        id = javaClass.simpleName,
        severity = Severity.Style,
        description = "Do not use isOne(), use isEqualTo(1) instead.",
        debt = Debt.FIVE_MINS,
    )

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)

        val selectorExpression = expression.selectorExpression ?: return
        val receiverExpression = expression.receiverExpression

        if (!receiverExpression.isAssertThat()) return

        if (selectorExpression.referenceExpression()?.text == "isOne") {
            report(
                CodeSmell(
                    issue = issue,
                    entity = Entity.from(expression),
                    message = issue.description,
                ),
            )
        }
    }
}
