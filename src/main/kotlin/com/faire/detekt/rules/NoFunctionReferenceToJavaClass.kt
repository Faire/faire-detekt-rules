package com.faire.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression

/**
 * No use of `::javaClass`; use `.javaClass` instead.
 *
 * `Foo::bar` gives you a reference to function/property `bar` on class `Foo`:
 * https://kotlinlang.org/docs/reflection.html#function-references
 * [javaClass] is an extension function on [Any]:
 * https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/java-class.html
 *
 * 99% of the time, you want to get the JVM class of some class/object instead of the callable reference to this
 * extension function. This bug can hide deep if you then proceed to call any method on [Class]: e.g.
 * ```
 * println(Foo::javaClass.simpleName) // gives you "javaClass"
 * println(Foo.javaClass.simpleName)  // gives you "Foo"
 * ```
 */
internal class NoFunctionReferenceToJavaClass(config: Config = Config.empty) : Rule(config) {
    override val issue = Issue(
        id = javaClass.simpleName,
        severity = Severity.CodeSmell,
        description = RULE_DESCRIPTION,
        debt = Debt.FIVE_MINS,
    )

    override fun visitCallableReferenceExpression(expression: KtCallableReferenceExpression) {
        super.visitCallableReferenceExpression(expression)
        if (expression.callableReference.getReferencedName() == "javaClass") {
            report(
                CodeSmell(
                    issue = issue,
                    entity = Entity.from(expression),
                    message = RULE_DESCRIPTION,
                ),
            )
        }
    }

    companion object {
        const val RULE_DESCRIPTION = "Do not call ::javaClass; did you mean .javaClass?"
    }
}
