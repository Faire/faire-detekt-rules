package com.faire.detekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
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
internal class NoExtensionFunctionOnNullableReceiver(config: Config = Config.empty) :
    Rule(config, "This rule reports extension functions on nullable types.") {
  override fun visitNamedFunction(function: KtNamedFunction) {
    super.visitNamedFunction(function)

    if (!function.isExtensionDeclaration()) return
    if (function.receiverTypeReference?.text?.endsWith("?") != true) return
    if (function.typeReference?.text?.endsWith("?") != true) return

    report(
        Finding(
            entity = Entity.from(function),
            message = "No extension functions on nullable types",
        ),
    )
  }
}
