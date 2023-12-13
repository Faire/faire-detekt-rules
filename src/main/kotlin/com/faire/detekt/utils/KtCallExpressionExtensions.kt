package com.faire.detekt.utils

import org.jetbrains.kotlin.psi.KtCallExpression

internal fun KtCallExpression.usesSizeProperty(): Boolean {
  if (valueArguments.size != 1) return false

  val argument = valueArguments.single().getArgumentExpression() ?: return false
  if (argument.children.size > 2) return false

  return argument.lastChild.text == "size"
}
