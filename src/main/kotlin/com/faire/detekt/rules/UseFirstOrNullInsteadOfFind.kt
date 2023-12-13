package com.faire.detekt.rules

import com.faire.detekt.utils.isTypeInClassFqNames
import com.faire.detekt.utils.isTypeResolutionAvailable
import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.astReplace
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression
import org.jetbrains.kotlin.resolve.calls.util.getType
import org.jetbrains.kotlin.types.KotlinType

private val bannedClasses = setOf(
    FqName("kotlin.String"),
    FqName("kotlin.collections.Iterable"),
)

/**
 * Instead of using find(), use firstOrNull() to retrieve
 * the first element in a collection that satisfies a certain condition
 *
 * Reasons:
 *  Readability: firstOrNull() clearly expresses the intent to find the first element
 *  that matches the specified condition or return null if no such element exists.
 *  find() has a similar behaviour, but isn't explicit about returning null when an
 *  element is not found.
 *
 *  Performance: firstOrNull() stops searching as soon as it finds the first matching
 *  element and returns it. This can be more efficient in scenarios where you only
 *  need to find the first occurrence that satisfies the condition and don't need to
 *  process the entire collection.
 */

@RequiresTypeResolution
internal class UseFirstOrNullInsteadOfFind(config: Config = Config.empty) : Rule(config) {
  override val issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.Style,
      description = "Use firstOrNull() instead of find()",
      debt = Debt.FIVE_MINS,
  )

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)
    if (!isTypeResolutionAvailable()) {
      return
    }

    val selectorExpression = expression.selectorExpression ?: return
    val receiverExpression = expression.receiverExpression

    if (selectorExpression.referenceExpression()?.text != "find") return
    val findExpression = selectorExpression as? KtCallExpression ?: return

    val receiverType = receiverExpression.getType(bindingContext) ?: return
    if (!isBannedClassType(receiverType)) return

    report(
        CodeSmell(
            issue = issue,
            entity = Entity.from(expression),
            message = issue.description,
        ),
    )

    withAutoCorrect {
      val arguments = if ((findExpression).lambdaArguments.isNotEmpty()) {
        " ${findExpression.lambdaArguments.joinToString { it.text }}"
      } else {
        "(${findExpression.valueArguments.joinToString { it.text }})"
      }

      findExpression.astReplace(KtPsiFactory(findExpression).createExpression("firstOrNull$arguments"))
    }
  }

  private fun isBannedClassType(type: KotlinType): Boolean {
    return type.isTypeInClassFqNames(bannedClasses)
  }
}
