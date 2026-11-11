package com.faire.detekt.rules

import com.faire.detekt.utils.getTypeName
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtNullableType
import org.jetbrains.kotlin.psi.KtTypeElement
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtUserType

/**
 * Reports function parameters and return types that are a Pair of two identical types, such as `Pair<Int, Int>`, or a
 * Pair with an `Any` type. A class with named properties makes clear which value is which.
 *
 * Type arguments are compared by their full text, ignoring nullability, so `Pair<Order.Token, Brand.Token>` is
 * allowed.
 */
internal class NoPairWithAmbiguousTypes(config: Config = Config.empty) :
    Rule(config, "This rule prevents developers from using Pair<T, T> or Pair with an Any type parameter") {
  override fun visitNamedFunction(function: KtNamedFunction) {
    super.visitNamedFunction(function)

    val offendingParameters = function.valueParameters.filter { it.typeReference?.isOffendingType() == true }

    for (offendingParameter in offendingParameters) {
      report(
          Finding(
              entity = Entity.from(function),
              message = "The function ${function.name} has parameter ${offendingParameter.name} which should be " +
                  "a class instead",
          ),
      )
    }

    if (function.typeReference?.isOffendingType() == true) {
      report(
          Finding(
              entity = Entity.from(function),
              message = "The function ${function.name} has a return type which should be a class instead",
          ),
      )
    }
  }
}

private fun KtTypeReference.isOffendingType(): Boolean {
  if (getTypeName() != "Pair") {
    return false
  }

  val genericTypes = this.typeElement?.getGenericTypes() ?: return false
  if (genericTypes.any { it.getTypeName() == "Any" }) {
    return true
  }

  val typeTexts = genericTypes.mapNotNull { it.textWithoutNullability() }
  return typeTexts.size == 2 && typeTexts[0] == typeTexts[1]
}

private fun KtTypeReference.textWithoutNullability(): String? {
  val element = typeElement
  val type = (element as? KtNullableType)?.innerType ?: element ?: return null
  return type.text.filterNot(Char::isWhitespace)
}

private fun KtTypeElement.getGenericTypes(): List<KtTypeReference> = when (this) {
    is KtUserType -> this.typeArgumentsAsTypes
    is KtNullableType -> this.typeArgumentsAsTypes
    else -> listOf()
  }.filterNotNull()
