package com.faire.detekt.utils

import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtModifierListOwner

/**
 * Stolen from the Detekt source code. These extensions to [KtModifierListOwner] may also be useful for our rules.
 */
internal fun KtModifierListOwner.isInternal(): Boolean = hasModifier(KtTokens.INTERNAL_KEYWORD)