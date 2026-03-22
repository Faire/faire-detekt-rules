package com.faire.detekt.rules

import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
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
        val modifierList = property.modifierList
        val internalNode = modifierList?.node?.findChildByType(KtTokens.INTERNAL_KEYWORD)
        val publicNode = modifierList?.node?.findChildByType(KtTokens.PUBLIC_KEYWORD)
        val existingVisibilityNode = internalNode ?: publicNode

        if (existingVisibilityNode != null) {
          // Replace existing visibility modifier text directly
          (existingVisibilityNode.psi as LeafPsiElement).rawReplaceWithText("private")
        } else if (modifierList != null) {
          // Has modifier list but no visibility keyword (e.g., "const val")
          val modifierListNode = modifierList.node
          modifierListNode.addChild(
            LeafPsiElement(KtTokens.PRIVATE_KEYWORD, "private"),
            modifierListNode.firstChildNode,
          )
          modifierListNode.addChild(
            PsiWhiteSpaceImpl(" "),
            modifierListNode.firstChildNode.treeNext,
          )
        } else {
          // No modifier list (e.g., bare "val bar = 1")
          val propertyNode = property.node
          propertyNode.addChild(
            LeafPsiElement(KtTokens.PRIVATE_KEYWORD, "private"),
            propertyNode.firstChildNode,
          )
          propertyNode.addChild(
            PsiWhiteSpaceImpl(" "),
            propertyNode.firstChildNode.treeNext,
          )
        }
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
