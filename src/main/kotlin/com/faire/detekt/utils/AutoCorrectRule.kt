package com.faire.detekt.utils

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import dev.detekt.api.Config
import dev.detekt.api.Rule
import dev.detekt.api.modifiedText
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile

/**
 * Base class for rules that auto-correct by replacing the text of PSI elements.
 *
 * detekt runs every auto-correcting rule against the same [KtFile] and writes [KtFile.modifiedText] to disk only after
 * all of them have run, so the PSI never shows what earlier rules replaced. The rules therefore share an edit log
 * through the file: each one logs its replacements at their PSI offsets, and the log rebuilds [KtFile.modifiedText]
 * from all of them.
 *
 * A replacement is skipped when it overlaps one already in the log, or when another tool, such as ktlint, changed the
 * file before the first replacement was logged and [TextMapping] cannot find the replaced text in that tool's output.
 * Its finding is still reported, and the next run fixes it.
 */
internal abstract class AutoCorrectRule(config: Config, description: String) : Rule(config, description) {

  private val replacements = mutableListOf<Replacement>()

  protected fun replace(element: KtElement, replacement: String) {
    replacements += Replacement(element.textRange, replacement)
  }

  final override fun preVisit(root: KtFile) {
    replacements.clear()
  }

  final override fun postVisit(root: KtFile) {
    if (replacements.isNotEmpty()) EditLog.of(root).add(replacements)
  }
}

private class Replacement(val range: TextRange, val text: String)

/** The replacements auto-correcting rules made to [file] during one detekt run. */
private class EditLog private constructor(private val file: KtFile) {
  private val originalText = file.text
  private val baseText = file.modifiedText ?: originalText
  private val toBaseText = TextMapping(originalText, baseText)

  /** Replacements at their offsets in [baseText], none of them overlapping. */
  private val applied = mutableListOf<Replacement>()

  private var text = baseText

  /** Logs the [replacements] that fit, innermost first, and rebuilds the text of [file] if there were any. */
  fun add(replacements: List<Replacement>) {
    val appliedBefore = applied.size
    val innermostFirst = compareByDescending<Replacement> { it.range.startOffset }.thenBy { it.range.endOffset }
    for (replacement in replacements.sortedWith(innermostFirst)) {
      val range = toBaseText.map(replacement.range)
      if (range != null && applied.none { it.range.intersectsStrict(range) }) {
        applied += Replacement(range, replacement.text)
      }
    }
    if (applied.size == appliedBefore) return

    text = rebuild()
    file.modifiedText = text
  }

  /** Whether [file] still has the text this log gave it, which ends once detekt writes it or anything else edits it. */
  private fun isCurrent(): Boolean = (file.modifiedText ?: originalText) === text && file.text == originalText

  private fun rebuild(): String = buildString(baseText.length) {
    var copiedUpTo = 0
    for (replacement in applied.sortedBy { it.range.startOffset }) {
      append(baseText, copiedUpTo, replacement.range.startOffset).append(replacement.text)
      copiedUpTo = replacement.range.endOffset
    }
    append(baseText, copiedUpTo, baseText.length)
  }

  companion object {
    private val KEY = Key<EditLog>("com.faire.detekt.utils.EditLog")

    /** Returns the log of [file], or starts a new one when the previous log is no longer current. */
    fun of(file: KtFile): EditLog =
        file.getUserData(KEY)?.takeIf { it.isCurrent() } ?: EditLog(file).also { file.putUserData(KEY, it) }
  }
}
