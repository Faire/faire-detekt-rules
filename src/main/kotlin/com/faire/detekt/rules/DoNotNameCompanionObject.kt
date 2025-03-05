package com.faire.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtObjectDeclaration

/**
 * This rule bans explicit naming of companion objects.
 *
 * Naming companion objects goes against the purpose of using them to group static properties under the outer class's
 * namespace and can cause issues since only one companion object is allowed per class.
 *
 * For an example of why this can be a problem, consider a class `Foo` with a companion object `Factory`
 *
 * ```
 * class Foo {
 *   companion object Factory {
 *     fun create(): Foo = Foo()
 *   }
 * }
 * ```
 *
 * By claiming the companion object as a factory, it makes it confusing to also use the companion object to group static
 * properties. For example, if we wanted to add a static constant `BAR` to the `Foo` class, we would either have to add
 * it in the `Factory` companion object — even though it is not related to instantiation — or create a new nested object
 * named something like `Constants` to hold it.
 *
 * ```
 * // Banned
 * class Foo {
 *   companion object Factory {
 *     fun create(): Foo = Foo()
 *   }
 *
 *   object Constants {
 *     const val BAR = "bar"
 *   }
 * }
 * ```
 *
 * It's preferable to use the companion object as a namespace for static properties and functions and avoid naming it,
 * while explicitly named nested objects such as `Factory` can be normal objects.
 *
 * ```
 * // Allowed
 * class Foo {
 *   object Factory {
 *     fun create(): Foo = Foo()
 *   }
 *
 *   companion object {
 *     const val BAR = "bar"
 *   }
 * }
 * ```
 */
internal class DoNotNameCompanionObject(config: Config = Config.empty) : Rule(config) {
  override val issue: Issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.Warning,
      description = "Companion objects should not be named",
      debt = Debt.FIVE_MINS,
  )

  override fun visitObjectDeclaration(declaration: KtObjectDeclaration) {
    super.visitObjectDeclaration(declaration)

    if (!declaration.isCompanion()) return
    if (declaration.nameIdentifier == null) return

    report(
        CodeSmell(
            issue = issue,
            entity = Entity.from(declaration),
            message = issue.description,
        ),
    )
  }
}
