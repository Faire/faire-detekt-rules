package com.faire.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
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
internal class UseSetInsteadOfListToSet(config: Config = Config.empty) : Rule(config) {
  override val issue: Issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.Style,
      description = "Use set() instead of list().toSet()",
      debt = Debt.FIVE_MINS,
  )

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return
    val receiverExpression = expression.receiverExpression

    if (selectorExpression.text != "toSet()") return

    if (receiverExpression.lastChild.text != "list()") return

    report(
        CodeSmell(
            issue = issue,
            entity = Entity.from(expression),
            message = issue.description,
        ),
    )

    withAutoCorrect {
      expression.lastChild.delete() // Delete "toSet()"
      expression.lastChild.delete() // Delete "."

      val expressionToReplace = receiverExpression.lastChild // "list()"

      expressionToReplace.astReplace(KtPsiFactory(expressionToReplace).createExpression("set()"))
    }
  }
}
