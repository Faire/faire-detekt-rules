package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRule
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.allChildren
import org.jetbrains.kotlin.psi.psiUtil.isExtensionDeclaration
import org.jetbrains.kotlin.psi.psiUtil.isPrivate
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifier

/**
 * No top-level non-private global variables.
 *
 * This is a convention we have chosen in order to keep our code consistent.
 *
 * Good:
 * ```
 * private const val INCHES_IN_A_FOOT = 12
 * class InchesConverter
 * ```
 *
 * Bad:
 * ```
 * const val INCHES_IN_A_FOOT = 12
 * class InchesConverter
 * ```
 */
internal class NoNonPrivateGlobalVariables(config: Config = Config.empty) :
    AutoCorrectRule(config, RULE_DESCRIPTION) {

  override fun visitProperty(property: KtProperty) {
    if (property.isTopLevel && !property.isPrivate() && !property.isExtensionDeclaration()) {
      report(Finding(entity = Entity.from(property), message = RULE_DESCRIPTION))

      if (autoCorrect) {
        pending.add(property.text to property.textWithPrivateVisibility())
      }
    }
  }

  private fun KtProperty.textWithPrivateVisibility(): String {
    val start = textRange.startOffset
    val visibility = visibilityModifier()
    if (visibility != null) {
      val range = visibility.textRange.shiftLeft(start)
      return text.replaceRange(range.startOffset, range.endOffset, "private")
    }
    // Insert after the KDoc and annotations so that the KDoc stays attached to the property.
    val firstKeyword = modifierList?.allChildren?.firstOrNull { it.node.elementType is KtModifierKeywordToken }
        ?: valOrVarKeyword
    val insertAt = firstKeyword.textRange.startOffset - start
    return text.replaceRange(insertAt, insertAt, "private ")
  }

  companion object {
    const val RULE_DESCRIPTION = "Global variables should be private"
  }
}
