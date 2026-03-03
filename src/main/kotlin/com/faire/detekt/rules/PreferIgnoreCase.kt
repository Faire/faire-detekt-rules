package com.faire.detekt.rules

import dev.detekt.api.Finding
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.astReplace
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

private val LOWERCASE_CALLS = setOf("lowercase()", "toLowerCase()")
private val IGNORE_CASE_FUNCTIONS = setOf(
    "contains",
    "startsWith",
    "endsWith",
    "indexOf",
    "indexOfAny",
    "lastIndexOf",
    "lastIndexOfAny",
)

/**
 * `ignoreCase` is preferred when doing case-insensitive string matching. Calling [toLowerCase] or [lowercase] creates
 * an unnecessary copy of the string.
 *
 * Good: `someString.contains("foo", ignoreCase = true)`
 * Bad: `someString.lowercase().contains("foo")`
 */
internal class PreferIgnoreCase(config: Config = Config.empty) : Rule(config, "use ignoreCase=true with various string matching functions without converting to lowercase") {
  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)
    val selectorExpression = expression.selectorExpression ?: return
    val receiverExpression = expression.receiverExpression
    if (receiverExpression.lastChild.text in LOWERCASE_CALLS &&
        selectorExpression.referenceExpression()?.text in IGNORE_CASE_FUNCTIONS
    ) {
      report(
          Finding(
              entity = Entity.from(expression),
              message = description,
          ),
      )
      if (autoCorrect) {
        val arguments = selectorExpression.lastChild
        if (arguments.text.last() != ')') {
          // Unknown format. Don't try to auto correct.
          return
        }
        val newArguments = arguments.text.dropLast(1) + ", ignoreCase = true)"
        arguments.astReplace(KtPsiFactory(expression).createCallArguments(newArguments))
        // We can't just delete receiverExpression.lastChild; it'd create an empty/invalid DotQualifiedExpression node.
        expression.firstChild.astReplace(receiverExpression.firstChild)
      }
    }
  }
}
