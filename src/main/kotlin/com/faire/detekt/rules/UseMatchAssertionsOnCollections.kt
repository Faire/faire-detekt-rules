package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRule
import com.faire.detekt.utils.hasReturnLabelled
import com.faire.detekt.utils.isAssertThat
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression

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
 * The rule only reports receivers that are an [Iterable], because AssertJ has no match assertions for maps, strings,
 * sequences or primitive arrays. It does not report `assertThat(collection.all { predicate }).isFalse()` or
 * `assertThat(collection.filterNot { predicate }).isNotEmpty()`, because no match assertion means "not all elements
 * match".
 *
 * Auto-correct skips predicates that use a label such as `return@all`: once the call is renamed to `allMatch`, that
 * label no longer refers to the predicate. The rule still reports these calls. To fix one by hand, change the label to
 * the new function name, such as `return@allMatch`.
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

    val assertionCall = expression.selectorExpression as? KtCallExpression ?: return
    if (assertionCall.valueArguments.isNotEmpty()) return
    val assertion = assertionCall.calleeExpression?.text ?: return

    val assertThatCall = expression.receiverExpression as? KtCallExpression ?: return
    if (!assertThatCall.isAssertThat()) return
    val assertThatArgument = assertThatCall.valueArguments.singleOrNull()
        ?.getArgumentExpression() as? KtDotQualifiedExpression ?: return

    val predicateCall = assertThatArgument.selectorExpression as? KtCallExpression ?: return
    if (predicateCall.valueArguments.size != 1) return
    val predicateFunction = predicateCall.calleeExpression?.text ?: return

    val matchAssertion = MATCH_ASSERTIONS[predicateFunction to assertion] ?: return
    if (!assertThatArgument.isCollectionsCallOnIterable(predicateCall)) return

    report(
        Finding(
            entity = Entity.from(expression),
            message = "Use assertThat(collection).$matchAssertion { predicate } instead of " +
                "assertThat(collection.$predicateFunction { predicate }).$assertion()",
        ),
    )

    if (!autoCorrect || predicateCall.hasReturnLabelled(predicateFunction)) return

    // The match assertions are Java methods, so a named argument such as `predicate = ...` would not compile
    val predicate = predicateCall.valueArguments.single().getArgumentExpression() ?: return
    val predicateText = if (predicate is KtLambdaExpression) " ${predicate.text}" else "(${predicate.text})"
    val collection = assertThatArgument.receiverExpression.text
    pending.add(expression.text to "assertThat($collection).$matchAssertion$predicateText")
  }

  private fun KtDotQualifiedExpression.isCollectionsCallOnIterable(call: KtCallExpression): Boolean = analyze(this) {
    val receiverType = receiverExpression.expressionType ?: return@analyze false
    val callableId = call.resolveToCall()?.successfulFunctionCallOrNull()?.symbol?.callableId
    callableId?.packageName == KOTLIN_COLLECTIONS_PACKAGE &&
        receiverType.isSubtypeOf(ITERABLE_CLASS_ID) &&
        !receiverType.isSubtypeOf(PATH_CLASS_ID)
  }
}
