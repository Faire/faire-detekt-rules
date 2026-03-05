package com.faire.detekt.rules

import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtParameterList
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType

private const val description = "Constructor parameter lists should not contain empty lines"

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
internal class NoEmptyLinesInConstructorParameters(config: Config = Config.empty) : Rule(config, description = description) {
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

    // Auto-correct by removing blank lines
    if (autoCorrect) {
        for (whitespaceNode in nodesWithBlankLines) {
          // Replace multiple consecutive newlines with a single newline
          // Preserve the indentation from the last line
          val lines = whitespaceNode.text.split("\n")
          val lastLine = lines.lastOrNull() ?: ""
          val correctedWhitespace = "\n$lastLine"

          whitespaceNode.rawReplaceWithText(correctedWhitespace)
        }
      }
  }
}
