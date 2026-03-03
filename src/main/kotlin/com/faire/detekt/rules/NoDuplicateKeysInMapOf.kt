package com.faire.detekt.rules

import dev.detekt.api.Finding
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtValueArgument

/**
 * This rule checks for duplicate keys in mapOf and mutableMapOf calls.
 *
 * ```
 * // Good
 * val map = mapOf(
 *     "key1" to "value1",
 *     "key2" to "value2",
 * )
 *
 * // Bad
 * val map = mapOf(
 *     "key1" to "value1",
 *     "key1" to "value2",
 * )
 * ```
 */
internal class NoDuplicateKeysInMapOf(config: Config = Config.empty) : Rule(config, "NoDuplicateKeysInMapOf") {
  override fun visitCallExpression(expression: KtCallExpression) {
    super.visitCallExpression(expression)

    val text = expression.calleeExpression?.text
    if (text == "mapOf" || text == "mutableMapOf") {
      val arguments = expression.valueArguments
      val keys = mutableSetOf<String>()

      for (argument in arguments) {
        val key = extractKey(argument)
        if (key != null) {
          if (!keys.add(key)) {
            report(
                Finding(
                    Entity.from(argument),
                    message = "The key $key is duplicated in the map.",
                ),
            )
          }
        }
      }
    }
  }

  private fun extractKey(argument: KtValueArgument): String? {
    val argumentExpression = argument.getArgumentExpression() ?: return null

    // Handle key value pairs
    if (argumentExpression is KtBinaryExpression) {
      val leftExpression = argumentExpression.left
      if (argumentExpression.operationReference.text != "to") {
        return null
      }
      return extractKeyFromExpression(leftExpression)
    }

    // Handle other expressions directly
    return extractKeyFromExpression(argumentExpression)
  }

  private fun extractKeyFromExpression(expression: KtExpression?): String? {
    // Skip function calls as they could generate unique values (e.g. random id generator)
    return if (expression == null || expression is KtCallExpression || expression is KtDotQualifiedExpression) {
      null
    } else {
      expression.text
    }
  }
}
