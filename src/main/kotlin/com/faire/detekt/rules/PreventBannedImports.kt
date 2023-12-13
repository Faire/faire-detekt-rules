package com.faire.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.astReplace
import org.jetbrains.kotlin.resolve.ImportPath

/**
 * Prevent a configurable list of imports, optionally with auto-correction to a configurable replacement import.
 *
 * This rule is similar to detekt's built-in ForbiddenImport, but this one supports autocorrect.
 *
 * Given the configuration `java.lang.Integer.max=kotlin.math.max`:
 * Good: `import kotlin.math.max`
 * Bad: `import java.lang.Integer.max`
 */
internal open class PreventBannedImports(config: Config = Config.empty) : Rule(config) {
  override val issue: Issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.Warning,
      description = "Prevent unwanted imports",
      debt = Debt.FIVE_MINS,
  )

  private val withAlternatives: Map<String, String> by lazy { getConfiguredWithAlternatives() }
  private val withoutAlternatives: List<String> by lazy { getConfiguredWithoutAlternatives() }

  protected open fun getConfiguredWithAlternatives(): Map<String, String> {
    val withAlternativesConfig: List<String> = valueOrDefault("withAlternatives", listOf())
    return withAlternativesConfig.asSequence()
        .map { it.split("=") }
        .map { Pair(it[0], it[1]) }
        .associate { it }
  }

  protected open fun getConfiguredWithoutAlternatives(): List<String> {
    return this.valueOrDefault("withoutAlternatives", listOf())
  }

  private fun maybeReportIssue(
      invalidImport: String,
      importDirective: KtImportDirective,
      importReference: String,
      message: String,
  ): Boolean {
    val trailingImport = importReference.removePrefix(invalidImport)
    if (trailingImport.isNotEmpty() && !trailingImport.startsWith(".")) return false

    report(
        CodeSmell(
            issue = issue,
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
        withAutoCorrect {
          val newImport = KtPsiFactory(importDirective)
              .createImportDirective(
                  ImportPath.fromString(
                      validImport + importReference.removePrefix(invalidImport),
                  ),
              )
          importDirective.astReplace(newImport)
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
