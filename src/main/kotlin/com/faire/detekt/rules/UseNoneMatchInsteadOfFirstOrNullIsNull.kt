package com.faire.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.astReplace
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

/**
 * Use `assertThat(collection).noneMatch { predicate }` instead of
 * `assertThat(collection.firstOrNull { predicate }).isNull()`.
 *
 * ```
 * // Good
 * assertThat(items).noneMatch { it.type == DELETED }
 *
 * // Bad
 * assertThat(items.firstOrNull { it.type == DELETED }).isNull()
 * ```
 *
 * The `.noneMatch { }` assertion is more idiomatic and provides clearer error messages
 * when the assertion fails, showing which elements matched rather than just indicating
 * that the result was not null.
 */
internal class UseNoneMatchInsteadOfFirstOrNullIsNull(config: Config = Config.empty) : Rule(config) {
  override val issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.Style,
      description = "Use assertThat(collection).noneMatch { predicate } instead of " +
          "assertThat(collection.firstOrNull { predicate }).isNull()",
      debt = Debt.FIVE_MINS,
  )

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return
    if (selectorExpression.text != "isNull()") return

    val receiverExpression = expression.receiverExpression
    val assertThatCall = receiverExpression as? KtCallExpression ?: return
    if (assertThatCall.referenceExpression()?.text != "assertThat") return

    val assertThatArgument = assertThatCall.valueArguments.singleOrNull()
        ?.getArgumentExpression() as? KtDotQualifiedExpression ?: return

    val firstOrNullCall = assertThatArgument.selectorExpression as? KtCallExpression ?: return
    if (firstOrNullCall.referenceExpression()?.text != "firstOrNull") return

    val hasLambdaOrFunctionArg = firstOrNullCall.lambdaArguments.isNotEmpty() ||
        firstOrNullCall.valueArguments.isNotEmpty()

    if (!hasLambdaOrFunctionArg) return
    if (firstOrNullCall.children.count { it is KtLambdaArgument } > 1) return

    report(
        CodeSmell(
            issue = issue,
            entity = Entity.from(expression),
            message = issue.description,
        ),
    )

    withAutoCorrect {
      // Get the predicate arguments from firstOrNull
      val predicateArgs = if (firstOrNullCall.lambdaArguments.isNotEmpty()) {
        " ${firstOrNullCall.lambdaArguments.joinToString { it.text }}"
      } else {
        "(${firstOrNullCall.valueArguments.joinToString { it.text }})"
      }

      // Replace the assertThat argument (collection.firstOrNull{}) with just the collection
      // This preserves the original formatting of the collection expression
      assertThatArgument.astReplace(assertThatArgument.receiverExpression)

      // Replace isNull() with noneMatch { predicate }
      val noneMatchExpression = KtPsiFactory(expression.project).createExpression(
          "noneMatch$predicateArgs",
      )
      selectorExpression.astReplace(noneMatchExpression)
    }
  }
}
