package com.faire.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.isExtensionDeclaration

/**
 * This rule reports extension functions on nullable types.
 * Exceptions are made for functions that return non-nullable types since they do not introduce nullability.
 *
 * The reason for this rule is that extension functions on nullable types can obscure the nullability of the receiver.
 *
 * ```
 * // Bad
 * fun String?.foo(): String? = this
 *
 * nonNullString.foo() // Return value is nullable which needs to be handled downstream
 *
 * // Good
 * fun String.foo(): String = this
 *
 * nonNullString.foo() // Return value stays non-null
 * nullableString?.foo() // Use null-safe operator to handle nullable receiver
 *
 * // Okay — function returns non-nullable type, essentially converting a nullable into a non-nullable type
 * fun String?.emptyIfNull(): String = this ?: ""
 * ```
 */
internal class NoExtensionFunctionOnNullableReceiver(config: Config = Config.empty) : Rule(config) {
  override val issue: Issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.Warning,
      description = "This rule reports extension functions on nullable types.",
      debt = Debt.FIVE_MINS,
  )

  override fun visitNamedFunction(function: KtNamedFunction) {
    super.visitNamedFunction(function)

    if (!function.isExtensionDeclaration()) return
    if (function.receiverTypeReference?.text?.endsWith("?") != true) return
    if (function.typeReference?.text?.endsWith("?") != true) return

    report(
        CodeSmell(
            issue = issue,
            entity = Entity.from(function),
            message = "No extension functions on nullable types",
        ),
    )
  }
}
