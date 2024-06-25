package com.faire.detekt.rules

import com.faire.detekt.utils.isAssertThat
import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.astReplace
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

/**
 * Do not assert isEqual on the result of single(). Instead, use containsOnly.
 *
 * ```
 * // Good
 * assertThat(listof("a")).containsOnly("a")
 *
 * // Bad
 * assertThat(listof("a").single()).isEqualTo("a")
 * ```
 */
internal class DoNotAssertIsEqualOnTheResultOfSingle(config: Config = Config.empty) : Rule(config) {
    override val issue = Issue(
        id = javaClass.simpleName,
        severity = Severity.Warning,
        description = "use containsOnly() instead of asserting isEqual() on the result of single()",
        debt = Debt.FIVE_MINS,
    )

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)

        val isEqualExpression = expression.selectorExpression ?: return
        val receiverExpression = expression.receiverExpression

        if (!receiverExpression.isAssertThat()) return
        if (isEqualExpression.referenceExpression()?.text != "isEqualTo") return

        val assertThatExpression = receiverExpression as? KtCallExpression ?: return
        val argumentForAssertThatExpression =
            assertThatExpression.valueArguments.singleOrNull()?.getArgumentExpression() ?: return
        if (!argumentForAssertThatExpression.callsSingleWithNoArgumentAtTheEnd()) return

        report(
            CodeSmell(
                issue = issue,
                entity = Entity.from(expression),
                message = "containsOnly should be used instead of asserting isEqual on the result of single()",
            ),
        )

        withAutoCorrect {
            argumentForAssertThatExpression.lastChild.delete() // Delete "single()"
            argumentForAssertThatExpression.lastChild.delete() // Delete "."

            val argumentsForIsEqualExpression = (isEqualExpression as? KtCallExpression)?.valueArgumentList?.text
            isEqualExpression.astReplace(
                KtPsiFactory(isEqualExpression).createExpression("containsOnly$argumentsForIsEqualExpression")
            )
        }
    }

    private fun KtExpression.callsSingleWithNoArgumentAtTheEnd(): Boolean {
        // If argument expression does actually end with single(), then this would mean that the whole expression is
        // a dot qualified expression with single as its selector expression
        val selectorExpression = (this as? KtDotQualifiedExpression)?.selectorExpression ?: return false

        // Check if the callee is single() with no arguments e.g. single { it > 0 }
        val referenceExpression = selectorExpression.referenceExpression()?.text
        val valueArguments = (selectorExpression as? KtCallExpression)?.valueArguments
        return referenceExpression == "single" && valueArguments?.isEmpty() == true
    }
}
