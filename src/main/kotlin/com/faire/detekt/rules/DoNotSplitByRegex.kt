package com.faire.detekt.rules

import com.faire.detekt.utils.isTypeResolutionAvailable
import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall

/**
 * Prefer using a string literal instead of a regex in [String.split].
 *
 * In Java, [String.split] only takes a regex, and there is a heuristic to use an optimized version if the regex is a
 * string literal. Kotlin, however, does not have that heuristic and encourages the explicit use of a string literal.
 * There is a HUGE performance penalty in terms of both CPU and memory.
 * https://youtrack.jetbrains.com/issue/KT-16661/Performance-overhead-in-string-splitting-in-Kotlin-versus-Java
 */
@RequiresTypeResolution
internal class DoNotSplitByRegex(config: Config = Config.empty) : Rule(config) {
  override val issue: Issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.Performance,
      description = "use string literals in split() whenever possible for better performance and less memory. " +
          "If you have to use regexes, suppress the rule by @Suppress(\"DoNotSplitByRegex\"), and make sure " +
          "the regex is initialized only once (i.e. statically).",
      debt = Debt.FIVE_MINS,
  )

  override fun visitCallExpression(expression: KtCallExpression) {
    super.visitCallExpression(expression)
    if (!isTypeResolutionAvailable()) {
      return
    }
    if (expression.referenceExpression()?.text != "split") {
      return
    }

    val call = expression.getResolvedCall(bindingContext) ?: return
    if (call.extensionReceiver?.type.toString() == "String" &&
        call.valueArguments.keys.any { it.type.toString() == "Regex" }) {
      report(
          CodeSmell(
              issue = issue,
              entity = Entity.from(expression),
              message = issue.description,
          ),
      )
    }
  }
}
