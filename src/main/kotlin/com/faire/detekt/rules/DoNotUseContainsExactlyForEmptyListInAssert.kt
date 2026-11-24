package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRule
import com.faire.detekt.utils.isAssertThat
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression

/**
 * In a test, we should always use isEmpty() instead of containsExactly() with no arguments when asserting on empty
 * collections.
 *
 * This is a convention we have chosen in order to keep our test assertions consistent.
 *
 * Good example:
 *
 * ```
 * fun `test usage`() {
 *     assertThat(myCollection).isEmpty()
 * }
 * ```
 *
 * Bad example:
 *
 * ```
 * fun `test usage`() {
 *     assertThat(myCollection).containsExactly()
 * }
 * ```
 *
 * AssertJ's isEmpty() returns nothing, so a containsExactly() that is followed by another call is reported but not
 * auto-corrected.
 */
internal class DoNotUseContainsExactlyForEmptyListInAssert(config: Config = Config.empty) :
    AutoCorrectRule(config, "Do not call containsExactly() with no arguments, call isEmpty().") {

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val containsExactlyExpression = expression.selectorExpression as? KtCallExpression ?: return
    if (containsExactlyExpression.calleeExpression?.text != "containsExactly") return
    if (containsExactlyExpression.valueArguments.isNotEmpty()) return
    if (!expression.receiverExpression.isAssertThat()) return

    report(
        Finding(
            entity = Entity.from(expression),
            message = description,
        ),
    )

    if (autoCorrect && !expression.isReceiverOfAnotherCall()) {
      // Replace the whole assertion rather than just the call, so that the replacement doesn't land on an earlier
      // containsExactly() in the file that was not auto-corrected.
      val assertion = expression.text
      pending.add(assertion to assertion.removeSuffix(containsExactlyExpression.text) + "isEmpty()")
    }
  }
}

private fun KtDotQualifiedExpression.isReceiverOfAnotherCall(): Boolean =
    (parent as? KtQualifiedExpression)?.receiverExpression == this
