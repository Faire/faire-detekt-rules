package com.faire.detekt.rules

import com.faire.detekt.utils.isAssertThat
import com.faire.detekt.utils.isTypeResolutionAvailable
import com.faire.detekt.utils.usesSizeProperty
import dev.detekt.api.Finding
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Rule
import org.jetbrains.kotlin.builtins.KotlinBuiltIns.isCollectionOrNullableCollection
import org.jetbrains.kotlin.builtins.KotlinBuiltIns.isMapOrNullableMap
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.util.getType
import org.jetbrains.kotlin.types.KotlinType

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
internal class DoNotUseSizePropertyInAssert(config: Config = Config.empty) : Rule(config, "Do not use size property in assertion, use hasSize() instead.") {
  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)
    if (!isTypeResolutionAvailable()) {
      return
    }

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
    val mapType = when (assertArgument) {
      // assertThat(size)
      is KtNameReferenceExpression -> getImplicitThisType(assertArgument)
      // assertThat(something.something.size)
//      is KtDotQualifiedExpression -> assertArgument.receiverExpression.getType(bindingContext)
      else -> null
    } ?: return
    if (!mapType.isCollectionOrMap()) {
      return
    }

    report(
        Finding(
            entity = Entity.from(expression),
            message = description,
        ),
    )
  }

  private fun getImplicitThisType(expression: KtNameReferenceExpression): KotlinType? {
//    val descriptor = bindingContext[BindingContext.REFERENCE_TARGET, expression] ?: return null
//
//    // For a member property or function, find its dispatch receiver (implicit "this")
//    val containingClassType = when (descriptor) {
//      is PropertyDescriptor -> descriptor.dispatchReceiverParameter?.type
//      is FunctionDescriptor -> descriptor.dispatchReceiverParameter?.type
//      else -> null
//    }
//
//    return containingClassType
    return null
  }
}

private fun KotlinType.isCollectionOrMap(): Boolean {
  return isExactlyCollectionOrMap() || constructor.supertypes.any { it.isExactlyCollectionOrMap() }
}

private fun KotlinType.isExactlyCollectionOrMap(): Boolean {
  return isCollectionOrNullableCollection(this) || isMapOrNullableMap(this)
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
