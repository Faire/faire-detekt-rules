package com.faire.detekt.utils

import com.intellij.openapi.util.TextRange

/**
 * Finds ranges of [old] in [new] through the text the two share, matched with Myers' diff.
 *
 * A range that overlaps a change has no match, and neither does any range when turning [old] into [new] takes more than
 * [maxEdits] character insertions and deletions.
 */
internal class TextMapping(old: String, new: String, maxEdits: Int = 2_000) {
  private val equalRuns = if (old == new) {
    listOf(EqualRun(oldStart = 0, newStart = 0, length = old.length, previous = null))
  } else {
    equalRuns(old, new, maxEdits)
  }

  fun map(range: TextRange): TextRange? {
    val run = equalRuns.firstOrNull { range.startOffset >= it.oldStart && range.endOffset <= it.oldStart + it.length }
    return run?.let { range.shiftRight(it.newStart - it.oldStart) }
  }
}

private class EqualRun(val oldStart: Int, val newStart: Int, val length: Int, val previous: EqualRun?)

/**
 * The runs of equal characters along a shortest edit script from [old] to [new], or none if that script has more than
 * [maxEdits] insertions and deletions.
 */
private fun equalRuns(old: String, new: String, maxEdits: Int): List<EqualRun> {
  // Both arrays are indexed by diagonal (old index - new index) + offset.
  val offset = maxEdits + 1
  val furthestOldIndex = IntArray(2 * offset + 1)
  val paths = arrayOfNulls<EqualRun>(2 * offset + 1)
  for (edits in 0..maxEdits) {
    for (diagonal in -edits..edits step 2) {
      val isInsertion = diagonal == -edits ||
          (diagonal != edits && furthestOldIndex[offset + diagonal - 1] < furthestOldIndex[offset + diagonal + 1])
      val from = offset + if (isInsertion) diagonal + 1 else diagonal - 1
      val start = if (isInsertion) furthestOldIndex[from] else furthestOldIndex[from] + 1
      var end = start
      while (end < old.length && end - diagonal < new.length && old[end] == new[end - diagonal]) end++
      furthestOldIndex[offset + diagonal] = end
      paths[offset + diagonal] = if (end > start) {
        EqualRun(oldStart = start, newStart = start - diagonal, length = end - start, previous = paths[from])
      } else {
        paths[from]
      }
      if (end >= old.length && end - diagonal >= new.length) {
        return generateSequence(paths[offset + diagonal]) { it.previous }.toList().asReversed()
      }
    }
  }
  return listOf()
}
