package com.faire.detekt.utils

import dev.detekt.api.Config
import dev.detekt.api.Rule
import dev.detekt.api.modifiedText
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile

/**
 * Base class for rules that auto-correct by rewriting the text of PSI elements.
 *
 * detekt runs every auto-correcting rule against the same [KtFile] and only writes [KtFile.modifiedText] to disk
 * afterwards, so a rule may receive a file whose text was already modified by other rules. PSI offsets are then no
 * longer reliable, which is why replacements are located by the occurrence index of the element's text instead.
 */
internal abstract class AutoCorrectRule(config: Config, description: String) : Rule(config, description) {

  private val replacements = mutableListOf<Replacement>()

  protected fun replace(element: KtElement, replacement: String) {
    replacements += Replacement(element.textRange.startOffset, element.text, replacement)
  }

  override fun preVisit(root: KtFile) {
    replacements.clear()
  }

  override fun postVisit(root: KtFile) {
    if (replacements.isEmpty()) return

    val originalText = root.text
    var text = root.modifiedText ?: originalText
    var replacedFromOffset = originalText.length
    // Replacing from the end of the file keeps the occurrence indexes of the remaining replacements valid.
    for (replacement in replacements.sortedByDescending { it.startOffset }) {
      // Skip replacements that overlap an applied one; a later run fixes them once the text is re-parsed.
      if (replacement.endOffset > replacedFromOffset) continue

      val occurrence = originalText.countOccurrences(replacement.original, before = replacement.startOffset)
      val index = text.indexOfOccurrence(replacement.original, occurrence)
      if (index < 0) continue

      text = text.replaceRange(index, index + replacement.original.length, replacement.replacement)
      replacedFromOffset = replacement.startOffset
    }
    root.modifiedText = text
  }

  private class Replacement(val startOffset: Int, val original: String, val replacement: String) {
    val endOffset: Int get() = startOffset + original.length
  }
}

private fun String.countOccurrences(substring: String, before: Int): Int {
  var count = 0
  var index = indexOf(substring)
  while (index in 0 until before) {
    count++
    index = indexOf(substring, index + 1)
  }
  return count
}

private fun String.indexOfOccurrence(substring: String, occurrence: Int): Int {
  var index = indexOf(substring)
  repeat(occurrence) {
    if (index < 0) return -1
    index = indexOf(substring, index + 1)
  }
  return index
}
