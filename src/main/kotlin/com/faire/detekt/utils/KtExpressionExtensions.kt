package com.faire.detekt.utils

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDeclarationWithBody
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.isAncestor
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

internal fun KtExpression.isAssertThat(): Boolean = referenceExpression()?.text == "assertThat"

/**
 * Returns the selector call when it calls [functionName] with exactly one argument, either in parentheses or as a
 * trailing lambda, e.g. `items.filter { it.isValid }` or `items.filter(::isValid)`.
 */
internal fun KtDotQualifiedExpression.singleArgumentCallOrNull(functionName: String): KtCallExpression? =
    (selectorExpression as? KtCallExpression)
        ?.takeIf { it.calleeExpression?.text == functionName && it.valueArguments.size == 1 }

/**
 * Whether this call contains a return to [label]. A lambda's implicit label is the name of the function it is passed
 * to, so renaming the call breaks such a return, or rebinds it to an outer lambda with the same label.
 */
internal fun KtCallExpression.hasReturnLabelled(label: String): Boolean =
    anyDescendantOfType<KtReturnExpression> { it.getLabelName() == label }

/**
 * Whether this expression contains a return that is not inside a function declared in it, such as `return@all` or a
 * `return` from the enclosing function through an inline lambda. Such a return breaks when this expression is passed
 * to a different or non-inline function.
 */
internal fun KtExpression.hasReturnOutsideOwnFunctions(): Boolean =
    anyDescendantOfType<KtReturnExpression> { returnExpression ->
      val function = returnExpression.parents.firstOrNull { it is KtDeclarationWithBody && it !is KtFunctionLiteral }
      function == null || !isAncestor(function)
    }

/**
 * The text of this predicate argument when it is passed to a Java method such as AssertJ's `noneMatch` instead, in a
 * call that starts where [newCall] starts. A lambda becomes a trailing lambda and anything else is passed in
 * parentheses. A named argument such as `predicate = ...` loses its name, because Java methods have no named
 * parameters.
 */
internal fun KtValueArgument.toJavaPredicateArgumentText(newCall: PsiElement): String? {
  val predicate = getArgumentExpression() ?: return null
  val predicateText = predicate.textReindentedFor(newCall)
  return if (this is KtLambdaArgument || predicate is KtLambdaExpression) " $predicateText" else "($predicateText)"
}

/**
 * Moves the lines after the first to the left by the indent this expression's line has over [newLine]'s line, so a
 * multi-line lambda keeps its shape after it moves to [newLine]. Multi-line strings would change, so they are kept.
 */
private fun KtExpression.textReindentedFor(newLine: PsiElement): String {
  val columns = lineIndent() - newLine.lineIndent()
  if (columns <= 0 || anyDescendantOfType<KtStringTemplateExpression> { '\n' in it.text }) return text
  val indent = " ".repeat(columns)
  return text.lines()
      .mapIndexed { index, line -> if (index == 0) line else line.removePrefix(indent) }
      .joinToString("\n")
}

private fun PsiElement.lineIndent(): Int {
  val fileText = containingFile.text
  val start = textRange.startOffset
  val lineStart = fileText.lastIndexOf('\n', start - 1) + 1
  return fileText.substring(lineStart, start).takeWhile { it == ' ' }.length
}
