package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRule
import com.faire.detekt.utils.hasReturnOutsideOwnFunctions
import com.faire.detekt.utils.isAssertThat
import com.faire.detekt.utils.toJavaPredicateArgumentText
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType

private val ASSERTJ_PACKAGE = FqName("org.assertj.core.api")
private val KOTLIN_COLLECTIONS_PACKAGE = FqName("kotlin.collections")
private val ITERABLE_CLASS_ID = ClassId.topLevel(FqName("kotlin.collections.Iterable"))

// AssertJ resolves assertThat(path) to its Path assertions, which have no match assertions.
private val PATH_CLASS_ID = ClassId.topLevel(FqName("java.nio.file.Path"))

private val MATCH_ASSERTIONS = mapOf(
    ("all" to "isTrue") to "allMatch",
    ("any" to "isTrue") to "anyMatch",
    ("none" to "isTrue") to "noneMatch",
    ("any" to "isFalse") to "noneMatch",
    ("none" to "isFalse") to "anyMatch",
    ("filter" to "isEmpty") to "noneMatch",
    ("filter" to "isNotEmpty") to "anyMatch",
    ("filterNot" to "isEmpty") to "allMatch",
)

/**
 * Use `assertThat(collection).allMatch { predicate }`, `anyMatch { predicate }` or `noneMatch { predicate }` instead
 * of asserting on the result of `all`, `any`, `none`, `filter` or `filterNot`.
 *
 * ```
 * // Good
 * assertThat(numbers).allMatch { it % 2 == 1 }
 * assertThat(numbers).anyMatch { it % 2 == 1 }
 * assertThat(numbers).noneMatch { it % 2 == 1 }
 *
 * // Bad
 * assertThat(numbers.all { it % 2 == 1 }).isTrue()
 * assertThat(numbers.filterNot { it % 2 == 1 }).isEmpty()
 * assertThat(numbers.any { it % 2 == 1 }).isTrue()
 * assertThat(numbers.none { it % 2 == 1 }).isFalse()
 * assertThat(numbers.filter { it % 2 == 1 }).isNotEmpty()
 * assertThat(numbers.none { it % 2 == 1 }).isTrue()
 * assertThat(numbers.any { it % 2 == 1 }).isFalse()
 * assertThat(numbers.filter { it % 2 == 1 }).isEmpty()
 * ```
 *
 * The match assertions say what is being checked, and when they fail, AssertJ prints the collection and the element
 * that broke the assertion instead of "expected true but was false".
 *
 * The rule only reports AssertJ's `assertThat` on receivers that are an [Iterable], because AssertJ has no match
 * assertions for maps, strings, sequences or primitive arrays. It does not report:
 * - `assertThat(collection.all { predicate }).isFalse()` or
 *   `assertThat(collection.filterNot { predicate }).isNotEmpty()`, because no match assertion means "not all elements
 *   match".
 * - Assertions with more assertions chained after them, such as
 *   `assertThat(collection.filter { predicate }).isNotEmpty().hasSize(2)`, because the chained assertions would then
 *   run on the whole collection instead of the matching elements.
 * - Predicates that call suspend functions, because the match assertions take a Java `Predicate`, which cannot call
 *   them.
 *
 * Auto-correct skips predicates that contain a return, unless it is inside a function declared in the predicate. A
 * label such as `return@all` no longer refers to the predicate once the call is renamed to `allMatch`, and a `return`
 * from the enclosing function only works in an inline lambda, which a Java `Predicate` is not. The rule still reports
 * these calls. To fix a labelled return by hand, change the label to the new function name, such as `return@allMatch`.
 */
internal class UseMatchAssertionsOnCollections(config: Config = Config.empty) :
    AutoCorrectRule(
        config,
        "Use assertThat(collection).allMatch, anyMatch or noneMatch instead of asserting on the result of " +
            "all, any, none, filter or filterNot",
    ),
    RequiresAnalysisApi {

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    if ((expression.parent as? KtQualifiedExpression)?.receiverExpression == expression) return

    val assertionCall = expression.selectorExpression as? KtCallExpression ?: return
    if (assertionCall.valueArguments.isNotEmpty()) return
    val assertion = assertionCall.calleeExpression?.text ?: return

    val assertThatCall = expression.receiverExpression as? KtCallExpression ?: return
    if (!assertThatCall.isAssertThat()) return
    val assertThatArgument = assertThatCall.valueArguments.singleOrNull()
        ?.getArgumentExpression() as? KtDotQualifiedExpression ?: return

    val predicateCall = assertThatArgument.selectorExpression as? KtCallExpression ?: return
    val predicateArgument = predicateCall.valueArguments.singleOrNull() ?: return
    val predicate = predicateArgument.getArgumentExpression() ?: return
    val predicateFunction = predicateCall.calleeExpression?.text ?: return

    val matchAssertion = MATCH_ASSERTIONS[predicateFunction to assertion] ?: return
    if (!hasMatchAssertion(assertThatCall, assertThatArgument, predicateCall, predicate)) return

    report(
        Finding(
            entity = Entity.from(expression),
            message = "Use assertThat(collection).$matchAssertion { predicate } instead of " +
                "assertThat(collection.$predicateFunction { predicate }).$assertion()",
        ),
    )

    if (!autoCorrect || predicate.hasReturnOutsideOwnFunctions()) return

    val predicateText = predicateArgument.toJavaPredicateArgumentText(newCall = expression) ?: return
    val collection = assertThatArgument.receiverExpression.text
    pending.add(expression.text to "assertThat($collection).$matchAssertion$predicateText")
  }

  private fun hasMatchAssertion(
      assertThatCall: KtCallExpression,
      assertThatArgument: KtDotQualifiedExpression,
      predicateCall: KtCallExpression,
      predicate: KtExpression,
  ): Boolean = analyze(assertThatCall) {
    fun KtCallExpression.resolvedSymbol() = resolveToCall()?.successfulFunctionCallOrNull()?.symbol

    val receiverType = assertThatArgument.receiverExpression.expressionType ?: return@analyze false
    assertThatCall.resolvedSymbol()?.callableId?.packageName == ASSERTJ_PACKAGE &&
        predicateCall.resolvedSymbol()?.callableId?.packageName == KOTLIN_COLLECTIONS_PACKAGE &&
        receiverType.isSubtypeOf(ITERABLE_CLASS_ID) &&
        !receiverType.isSubtypeOf(PATH_CLASS_ID) &&
        !predicate.anyDescendantOfType<KtCallExpression> {
          (it.resolvedSymbol() as? KaNamedFunctionSymbol)?.isSuspend == true
        }
  }
}
