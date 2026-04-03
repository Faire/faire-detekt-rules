package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRule
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import org.jetbrains.kotlin.psi.KtParameterList
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType

private val BLANK_LINE_REGEX = Regex("""\n([ \t]*\n)+""")

/**
 * Detects and removes empty lines within constructor parameter lists.
 *
 * Empty lines within constructor parameter lists are visually noisy and inconsistent with code style.
 * This rule flags blank lines between parameters, trailing blank lines before the closing parenthesis,
 * and leading blank lines after the opening parenthesis.
 *
 * If you feel that blank lines are necessary for readability in certain cases (e.g., separating groups
 * of parameters in a large data class), consider using nested data classes. As a last resort, you can
 * delimit each group by a comment.
 *
 * ```
 * // Good
 * class Person(
 *   val firstName: String,
 *   val lastName: String,
 *   val age: Int,
 * )
 *
 * class Person(
 *   val firstName: String,
 *   val lastName: String,
 *   // some comment
 *   val age: Int,
 * )
 *
 * // Bad - blank line between parameters
 * class Person(
 *   val firstName: String,
 *
 *   val lastName: String,
 *   val age: Int,
 * )
 *
 * // Bad - trailing blank line
 * class Person(
 *   val firstName: String,
 *   val lastName: String,
 *
 * )
 * ```
 */
internal class NoEmptyLinesInConstructorParameters(config: Config = Config.empty) :
    AutoCorrectRule(config, description = "Constructor parameter lists should not contain empty lines") {

  override fun visitPrimaryConstructor(constructor: KtPrimaryConstructor) {
    super.visitPrimaryConstructor(constructor)
    checkConstructorParameters(constructor.valueParameterList)
  }

  override fun visitSecondaryConstructor(constructor: KtSecondaryConstructor) {
    super.visitSecondaryConstructor(constructor)
    checkConstructorParameters(constructor.valueParameterList)
  }

  private fun checkConstructorParameters(parameterList: KtParameterList?) {
    if (parameterList == null) return

    // Skip single-line parameter lists - no empty lines possible
    if (!parameterList.text.contains("\n")) return

    // Find all whitespace nodes that contain blank lines (consecutive newlines or whitespace-only lines)
    val whitespaceNodes = parameterList.getChildrenOfType<PsiWhiteSpaceImpl>()
    val nodesWithBlankLines = whitespaceNodes.filter { node ->
      // Detect patterns like "\n\n" or "\n  \n" or "\n\t\n" (whitespace-only lines)
      node.text.contains(Regex("""\n[ \t]*\n"""))
    }

    if (nodesWithBlankLines.isEmpty()) return

    // Report a code smell for the parameter list
    report(
        Finding(entity = Entity.from(parameterList), message = description),
    )

    if (autoCorrect) {
      val originalText = parameterList.text
      // Collapse blank lines: replace sequences of newlines (with optional whitespace) with a single newline
      val fixedText = BLANK_LINE_REGEX.replace(originalText, "\n")
      pending.add(originalText to fixedText)
    }
  }
}
