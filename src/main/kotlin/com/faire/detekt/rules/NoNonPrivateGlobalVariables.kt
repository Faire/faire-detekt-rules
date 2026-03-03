package com.faire.detekt.rules

import dev.detekt.api.Finding
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Rule
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.isExtensionDeclaration
import org.jetbrains.kotlin.psi.psiUtil.isPrivate

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
internal class NoNonPrivateGlobalVariables(config: Config = Config.empty) : Rule(config, RULE_DESCRIPTION) {
  override fun visitProperty(property: KtProperty) {
    if (property.isTopLevel && !property.isPrivate() && !property.isExtensionDeclaration()) {
      report(
          Finding(
              entity = Entity.from(property),
              message = RULE_DESCRIPTION,
          ),
      )

//      if (autoCorrect) {
//        if (!property.isSuppressedBy(ruleId, aliases)) {
//          if (property.isInternal()) {
//            property.removeModifier(KtTokens.INTERNAL_KEYWORD)
//          }
//
//          property.addModifier(KtTokens.PRIVATE_KEYWORD)
//        }
//      }
    }
  }

  companion object {
    const val RULE_DESCRIPTION = "Global variables should be private"
  }
}
