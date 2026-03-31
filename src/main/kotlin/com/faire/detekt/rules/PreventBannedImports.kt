package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRule
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import org.jetbrains.kotlin.psi.KtImportDirective

/**
 * Prevent a configurable list of imports, optionally with auto-correction to a configurable replacement import.
 *
 * This rule is similar to detekt's built-in ForbiddenImport, but this one supports autocorrect.
 *
 * Given the configuration `java.lang.Integer.max=kotlin.math.max`:
 * Good: `import kotlin.math.max`
 * Bad: `import java.lang.Integer.max`
 */
internal open class PreventBannedImports(config: Config = Config.empty) :
    AutoCorrectRule(config, "Prevent unwanted imports") {
  private val withAlternatives: Map<String, String> by lazy { getConfiguredWithAlternatives() }
  private val withoutAlternatives: List<String> by lazy { getConfiguredWithoutAlternatives() }

  protected open fun getConfiguredWithAlternatives(): Map<String, String> {
    val withAlternativesConfig: List<String> = config.valueOrDefault("withAlternatives", listOf())
    return withAlternativesConfig.asSequence()
        .map { it.split("=") }
        .map { Pair(it[0], it[1]) }
        .associate { it }
  }

  protected open fun getConfiguredWithoutAlternatives(): List<String> =
      config.valueOrDefault("withoutAlternatives", listOf())

  private fun maybeReportIssue(
      invalidImport: String,
      importDirective: KtImportDirective,
      importReference: String,
      message: String,
  ): Boolean {
    val trailingImport = importReference.removePrefix(invalidImport)
    if (trailingImport.isNotEmpty() && !trailingImport.startsWith(".")) return false

    report(
        Finding(
            entity = Entity.from(importDirective),
            message = message,
        ),
    )
    return true
  }

  override fun visitImportDirective(importDirective: KtImportDirective) {
    val importReference = importDirective.importedReference?.text ?: return
    for ((invalidImport, validImport) in withAlternatives) {
      if (importReference.startsWith(invalidImport)) {
        val didReportIssue = maybeReportIssue(
            invalidImport = invalidImport,
            importDirective = importDirective,
            importReference = importReference,
            message = "Replace $invalidImport import with $validImport",
        )
        if (!didReportIssue) continue
        if (autoCorrect) {
          val newImportPath = validImport + importReference.removePrefix(invalidImport)
          pending.add(importDirective.text to "import $newImportPath")
        }
      }
    }

    for (invalidImport in withoutAlternatives) {
      if (importReference.startsWith(invalidImport)) {
        maybeReportIssue(
            invalidImport = invalidImport,
            importDirective = importDirective,
            importReference = importReference,
            message = "Do not import $invalidImport",
        )
      }
    }
  }
}
