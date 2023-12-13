package com.faire.detekt.utils

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.astReplace
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

internal fun KtExpression.isAssertThat(): Boolean {
  return referenceExpression()?.text == "assertThat"
}

internal fun KtExpression.simplifyCollectionPatterns(replacedWith: String) {
  val filterExpression = this.lastChild

  val arguments = if ((filterExpression as KtCallExpression).lambdaArguments.isNotEmpty()) {
    " ${filterExpression.lambdaArguments.joinToString { it.text }}"
  } else {
    "(${filterExpression.valueArguments.joinToString { it.text }})"
  }

  filterExpression.astReplace(KtPsiFactory(filterExpression).createExpression("$replacedWith$arguments"))
}
