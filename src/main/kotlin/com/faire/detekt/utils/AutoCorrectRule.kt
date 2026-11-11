package com.faire.detekt.utils

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import dev.detekt.api.Config
import dev.detekt.api.Rule
import dev.detekt.api.modifiedText
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.UserDataProperty

/**
 * Base class for rules that auto-correct by replacing the text of PSI elements.
 *
 * detekt runs every auto-correcting rule against the same [KtFile] and writes [KtFile.modifiedText] to disk only after
 * all of them have run, so the PSI never shows what earlier rules replaced. The rules therefore share an edit log
 * through the file: each one logs its replacements at their PSI offsets, then rebuilds [KtFile.modifiedText] from the
 * whole log.
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

  override fun preVisit(root: KtFile) {
    replacements.clear()
  }

  override fun postVisit(root: KtFile) {
    if (replacements.isEmpty()) return

    val editLog = root.editLog?.takeIf { it.isCurrentFor(root) } ?: EditLog(root).also { root.editLog = it }
    if (editLog.add(replacements)) root.modifiedText = editLog.text
  }
}

private var KtFile.editLog: EditLog? by UserDataProperty(Key("com.faire.detekt.utils.AutoCorrectRule.editLog"))

private class Replacement(val range: TextRange, val text: String)

/** The replacements auto-correcting rules made to a file during one detekt run. */
private class EditLog(root: KtFile) {
  private val originalText = root.text
  private val baseText = root.modifiedText ?: originalText
  private val toBaseText = TextMapping(originalText, baseText)

  /** Replacements at their offsets in [baseText], none of them overlapping. */
  private val applied = mutableListOf<Replacement>()

  var text: String = baseText
    private set

  /** Whether [root] still has the text this log gave it, which ends once detekt writes it or anything else edits it. */
  fun isCurrentFor(root: KtFile): Boolean = (root.modifiedText ?: originalText) === text && root.text == originalText

  /** Logs the [replacements] that fit, innermost first, and returns whether there were any. */
  fun add(replacements: List<Replacement>): Boolean {
    val innermostFirst = compareByDescending<Replacement> { it.range.startOffset }.thenBy { it.range.endOffset }
    var added = false
    for (replacement in replacements.sortedWith(innermostFirst)) {
      val range = toBaseText.map(replacement.range)
      if (range != null && applied.none { it.range.intersectsStrict(range) }) {
        applied += Replacement(range, replacement.text)
        added = true
      }
    }
    if (added) text = rebuild()
    return added
  }

  private fun rebuild(): String = buildString(baseText.length) {
    var copiedUpTo = 0
    for (replacement in applied.sortedBy { it.range.startOffset }) {
      append(baseText, copiedUpTo, replacement.range.startOffset).append(replacement.text)
      copiedUpTo = replacement.range.endOffset
    }
    append(baseText, copiedUpTo, baseText.length)
  }
}
