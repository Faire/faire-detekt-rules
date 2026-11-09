package com.faire.detekt.utils

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

internal fun KtExpression.isAssertThat(): Boolean = referenceExpression()?.text == "assertThat"

/**
 * Returns the selector call when it calls [functionName] with exactly one argument, either in parentheses or as a
 * trailing lambda, e.g. `items.filter { it.isValid }` or `items.filter(::isValid)`.
 */
internal fun KtDotQualifiedExpression.singleArgumentCallOrNull(functionName: String): KtCallExpression? =
    (selectorExpression as? KtCallExpression)
        ?.takeIf { it.calleeExpression?.text == functionName && it.valueArguments.size == 1 }
