package com.faire.detekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression

/**
 * Do not call `::javaClass`.
 *
 * `Foo::bar` gives you a reference to function/property `bar` on class `Foo`:
 * https://kotlinlang.org/docs/reflection.html#function-references
 * [javaClass] is an extension function on [Any]:
 * https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/java-class.html
 *
 * 99% of the time, you want to get the JVM class of some class/object instead of the callable reference to this
 * extension function. This bug can hide deep if you then proceed to access a name that exists on both
 * [java.base.Class] and [kotlin.reflect.KCallable] : e.g.
 * ```
 * class Foo
 * val foo = Foo()
 * println(Foo::javaClass.name) // gives you "javaClass"
 * println(foo.javaClass.name)  // gives you "Foo"
 * ```
 */
internal class NoFunctionReferenceToJavaClass(config: Config = Config.empty) : Rule(config, RULE_DESCRIPTION) {
  override fun visitCallableReferenceExpression(expression: KtCallableReferenceExpression) {
    super.visitCallableReferenceExpression(expression)
    if (expression.callableReference.getReferencedName() == "javaClass") {
      report(
          Finding(
              entity = Entity.from(expression),
              message = RULE_DESCRIPTION,
          ),
      )
    }
  }

  companion object {
    const val RULE_DESCRIPTION = "Do not call ::javaClass; " +
        "did you mean someObject.javaClass or SomeClass::class.java?"
  }
}
