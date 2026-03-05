package com.faire.detekt.rules

import com.faire.detekt.utils.isAssertThat
import com.faire.detekt.utils.usesSizeProperty
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.successfulVariableAccessCall
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

private val COLLECTION_CLASS_ID = ClassId.topLevel(FqName("kotlin.collections.Collection"))
private val MAP_CLASS_ID = ClassId.topLevel(FqName("kotlin.collections.Map"))

/**
 * In a test, we should always use the hasSize() assertion method instead of comparing the size property to some value.
 *
 * One exception to using hasSize() is that it should not be used with an argument of zero. Use isEmpty() instead.
 * See [DoNotUseHasSizeForEmptyListInAssert].
 *
 * This is a convention we have chosen in order to keep our test assertions consistent.
 *
 * Good example:
 *
 * ```
 * fun `test usage`() {
 *     assertThat(myCollection).hasSize(3)
 * }
 * ```
 *
 * Bad example:
 *
 * ```
 * fun `test usage`() {
 *     assertThat(myCollection.size).isEqualTo(3)
 * }
 * ```
 */
internal class DoNotUseSizePropertyInAssert(config: Config = Config.empty) : Rule(config, "Do not use size property in assertion, use hasSize() instead."), RequiresAnalysisApi {
  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)

    val selectorExpression = expression.selectorExpression ?: return
    val receiverExpression = expression.receiverExpression

    if (!receiverExpression.isAssertThat()) return

    val selectorExpressionText = selectorExpression.referenceExpression()?.text
    if (selectorExpressionText != "isEqualTo" && selectorExpressionText != "isZero") return

    val assertExpression = receiverExpression as? KtCallExpression ?: return
    val isEqualToOrIsZeroExpression = selectorExpression as? KtCallExpression ?: return

    if (!assertExpression.usesSizeProperty() || !isEqualToOrIsZeroExpression.numericComparison()) {
      return
    }

    val assertArgument = assertExpression.valueArguments.singleOrNull()?.getArgumentExpression() ?: return

    analyze(expression) {
      val sizeReceiverType: KaType? = when (assertArgument) {
        // assertThat(size) — implicit receiver in with block or extension function
        is KtNameReferenceExpression -> {
          val call = assertArgument.resolveToCall()?.successfulVariableAccessCall()
          call?.partiallyAppliedSymbol?.dispatchReceiver?.type
              ?: call?.partiallyAppliedSymbol?.extensionReceiver?.type
        }
        // assertThat(something.size)
        is KtDotQualifiedExpression -> assertArgument.receiverExpression.expressionType
        else -> null
      }

      if (sizeReceiverType == null) return@analyze
      if (!sizeReceiverType.isSubtypeOf(COLLECTION_CLASS_ID) && !sizeReceiverType.isSubtypeOf(MAP_CLASS_ID)) {
        return@analyze
      }

      report(
          Finding(
              entity = Entity.from(expression),
              message = description,
          ),
      )
    }
  }
}

private fun KtCallExpression.numericComparison(): Boolean {
  if (firstChild.text == "isZero") {
    return true
  } else if (valueArguments.size != 1) {
    return false
  }

  val stringToCheck = valueArguments.single().text.trimEnd('L')

  return stringToCheck.toLongOrNull() != null
}
