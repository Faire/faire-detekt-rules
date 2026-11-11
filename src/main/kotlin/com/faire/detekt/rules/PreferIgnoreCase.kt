package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRule
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtEscapeStringTemplateEntry
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
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
 *
 * Auto-correct only rewrites calls with a single argument that is known to be lowercase: a lowercase literal or a
 * lowercase() call. For any other argument, ignoring case could change the result, so the call is only reported.
 */
internal class PreferIgnoreCase(config: Config = Config.empty) :
    AutoCorrectRule(
        config,
        "use ignoreCase=true with various string matching functions without converting to lowercase",
    ) {

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
        val call = selectorExpression as? KtCallExpression ?: return
        val argumentList = call.valueArgumentList ?: return
        val argument = argumentList.arguments.singleOrNull() ?: return
        if (argument.getArgumentExpression()?.isKnownLowercase() != true) return

        val insertAt = argument.textRangeInParent.endOffset
        val newArguments = argumentList.text.replaceRange(insertAt, insertAt, ", ignoreCase = true")
        val functionName = selectorExpression.referenceExpression()?.text ?: return
        // Strip .lowercase()/.toLowerCase() from receiver to get the base string expression
        val baseReceiver = receiverExpression.firstChild.text

        pending.add(expression.text to "$baseReceiver.$functionName$newArguments")
      }
    }
  }

  private fun KtExpression.isKnownLowercase(): Boolean = when (this) {
    is KtStringTemplateExpression -> !hasInterpolation() && entries
        .joinToString("") { entry -> (entry as? KtEscapeStringTemplateEntry)?.unescapedValue ?: entry.text }
        .let { value -> value == value.lowercase() }
    is KtConstantExpression -> node.elementType == KtNodeTypes.CHARACTER_CONSTANT &&
        '\\' !in text && text == text.lowercase()
    is KtDotQualifiedExpression -> selectorExpression?.text in LOWERCASE_CALLS
    else -> false
  }
}
