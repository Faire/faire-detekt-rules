package com.faire.detekt.utils

import dev.detekt.api.Config
import dev.detekt.api.Rule
import dev.detekt.api.modifiedText
import org.jetbrains.kotlin.psi.KtFile

internal abstract class AutoCorrectRule(config: Config, description: String) : Rule(config, description) {

  protected val pending = mutableListOf<Pair<String, String>>()

  override fun preVisit(root: KtFile) {
    pending.clear()
  }

  override fun postVisit(root: KtFile) {
    if (pending.isEmpty()) return
    var text = root.modifiedText ?: root.text
    for ((original, replacement) in pending) {
      text = text.replaceFirst(original, replacement)
    }
    root.modifiedText = text
  }
}
