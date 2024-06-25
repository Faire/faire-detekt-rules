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

        val selectorExpression = expression.selectorExpression ?: return
        val receiverExpression = expression.receiverExpression

        if (!receiverExpression.isAssertThat()) return
        if (selectorExpression.referenceExpression()?.text != "isEqualTo") return

        val assertThatExpression = receiverExpression as? KtCallExpression ?: return
        if (!assertThatExpression.isAssertingOnResultOfSingle()) return

        report(
            CodeSmell(
                issue = issue,
                entity = Entity.from(expression),
                message = "containsOnly should be used instead of asserting isEqual on the result of single()",
            ),
        )
    }

    private fun KtCallExpression.isAssertingOnResultOfSingle(): Boolean {
        val argument = valueArguments.singleOrNull() ?: return false

        return argument.text.endsWith(".single()")
    }
}
