package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRule
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import org.jetbrains.kotlin.psi.KtCallExpression

private val EMPTY_COLLECTIONS = setOf("emptyList", "emptySet", "emptyMap")

/**
 * Instead of using `emptyMap()`, `emptyList()` or `emptySet()` we should use `mapOf()`, `listOf()`, or `setOf()`.
 *
 * This code convention exists for the purpose of consistency.
 */
internal class UseOfCollectionInsteadOfEmptyCollection(config: Config = Config.empty) :
    AutoCorrectRule(config, ISSUE) {

  override fun visitCallExpression(expression: KtCallExpression) {
    super.visitCallExpression(expression)

    val calleeText = expression.calleeExpression?.text ?: return
    if (calleeText !in EMPTY_COLLECTIONS) return

    report(
        Finding(
            entity = Entity.from(expression),
            message = description,
        ),
    )

    if (autoCorrect) {
      val replacement = when (calleeText) {
        "emptyList" -> "listOf"
        "emptySet" -> "setOf"
        "emptyMap" -> "mapOf"
        else -> return
      }
      val typeArgs = expression.typeArgumentList?.text.orEmpty()
      pending.add(expression.text to "$replacement$typeArgs()")
    }
  }

  companion object {
    const val ISSUE = "replace emptySet(), emptyList(), or emptyMap() with setOf(), listOf(), or mapOf() respectively"
  }
}
