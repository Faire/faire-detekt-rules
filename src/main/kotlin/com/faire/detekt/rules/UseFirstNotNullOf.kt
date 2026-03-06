package com.faire.detekt.rules

import com.faire.detekt.utils.simplifyCollectionPatterns
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

private val MAP_NOT_NULL_REGEX = """.*\.*mapNotNull\s*[{(].+""".toRegex()

/**
 * Instead of using `.mapNotNull { ... }.first()`, use `firstNotNullOf { ... }` to transform and select
 * an element in a collection simultaneously.
 *
 * Reasons:
 *  Performance: `firstNotNullOf {}` processes each element at most once and early returns if it finds a not-null
 *               result after the transformation. On the other hand, if not used on a sequence, `.mapNotNull {}.first()`
 *               processes the entire collection, which performs transformations on elements we immediately throw away.
 *
 *  Readability: `firstNotNullOf {}` clearly expresses the intent to select the first element after the transformation.
 *               Using `.mapNotNull { ... }.first()` is more verbose.
 */
internal class UseFirstNotNullOf(config: Config = Config.empty) :
    Rule(config, "use firstNotNullOf() instead of mapNotNull followed by first()") {
  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return
    if (selectorExpression.text != "first()") return

    val receiverExpression = expression.receiverExpression as? KtDotQualifiedExpression ?: return
    if (!receiverExpression.lastChild.text.matches(MAP_NOT_NULL_REGEX)) return

    report(
        Finding(
            entity = Entity.from(expression),
            message = description,
        ),
    )

    if (autoCorrect) {
      removeCallToFirst(expression)
      mayBeRemoveCallToAsSequence(receiverExpression)

      receiverExpression.simplifyCollectionPatterns("firstNotNullOf")
    }
  }
}

private fun removeCallToFirst(expression: KtDotQualifiedExpression) {
  expression.lastChild.delete() // Delete "first()"
  expression.lastChild.delete() // Delete "."
}

private fun mayBeRemoveCallToAsSequence(expression: KtDotQualifiedExpression) {
  // Check whether the child immediately preceding the receiver expression is "asSequence()"
  val receiverExpression = expression.receiverExpression
  if (receiverExpression.lastChild.text != "asSequence()") return

  receiverExpression.lastChild.delete() // Delete "asSequence()"
  receiverExpression.lastChild.delete() // Delete "."
}
