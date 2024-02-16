package com.faire.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.internal.isSuppressedBy
import io.gitlab.arturbosch.detekt.rules.isInternal
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
internal class NoNonPrivateGlobalVariables(config: Config = Config.empty) : Rule(config) {
  override val issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.CodeSmell,
      description = RULE_DESCRIPTION,
      debt = Debt.FIVE_MINS,
  )

  override fun visitProperty(property: KtProperty) {
    if (property.isTopLevel && !property.isPrivate() && !property.isExtensionDeclaration()) {
      report(
          CodeSmell(
              issue = issue,
              entity = Entity.from(property),
              message = RULE_DESCRIPTION,
          ),
      )

      withAutoCorrect {
        if (!property.isSuppressedBy(ruleId, aliases)) {
          if (property.isInternal()) {
            property.removeModifier(KtTokens.INTERNAL_KEYWORD)
          }

          property.addModifier(KtTokens.PRIVATE_KEYWORD)
        }
      }
    }
  }

  companion object {
    const val RULE_DESCRIPTION = "Global variables should be private"
  }
}
