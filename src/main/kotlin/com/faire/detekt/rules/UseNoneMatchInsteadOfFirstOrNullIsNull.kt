package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRule
import com.faire.detekt.utils.hasReturnLabelled
import com.faire.detekt.utils.toJavaPredicateArgumentText
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument
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
 *
 * Auto-correct skips predicates that use `return@firstOrNull`: once the call is renamed to
 * `noneMatch`, that label no longer refers to the predicate. The rule still reports these
 * calls. To fix one by hand, change the label to `return@noneMatch`.
 */
internal class UseNoneMatchInsteadOfFirstOrNullIsNull(config: Config = Config.empty) :
    AutoCorrectRule(
        config,
        "Use assertThat(collection).noneMatch { predicate } instead of " +
            "assertThat(collection.firstOrNull { predicate }).isNull()",
    ) {

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
        Finding(
            entity = Entity.from(expression),
            message = description,
        ),
    )

    if (autoCorrect && !firstOrNullCall.hasReturnLabelled("firstOrNull")) {
      val collectionExpression = assertThatArgument.receiverExpression.text
      val predicateArgs = firstOrNullCall.valueArguments.singleOrNull()
          ?.toJavaPredicateArgumentText(newCall = expression) ?: return

      pending.add(expression.text to "assertThat($collectionExpression).noneMatch$predicateArgs")
    }
  }
}
