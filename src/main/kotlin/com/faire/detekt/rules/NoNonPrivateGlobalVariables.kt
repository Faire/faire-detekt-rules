package com.faire.detekt.rules

import dev.detekt.api.Finding
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Rule
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtFile
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
  private val violations = mutableListOf<KtProperty>()

  override fun visitKtFile(file: KtFile) {
    violations.clear()
    super.visitKtFile(file)

    // Report all violations first using original offsets, then autocorrect in reverse
    // order so that modifications to later properties don't invalidate earlier positions.
    for (property in violations) {
      report(Finding(entity = Entity.from(property), message = RULE_DESCRIPTION))
    }

    if (autoCorrect) {
      for (property in violations.reversed()) {
        property.removeModifier(KtTokens.INTERNAL_KEYWORD)
        property.removeModifier(KtTokens.PUBLIC_KEYWORD)
        property.addModifier(KtTokens.PRIVATE_KEYWORD)
      }
    }
  }

  override fun visitProperty(property: KtProperty) {
    if (property.isTopLevel && !property.isPrivate() && !property.isExtensionDeclaration()) {
      violations.add(property)
    }
  }

  companion object {
    const val RULE_DESCRIPTION = "Global variables should be private"
  }
}
