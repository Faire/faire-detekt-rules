package com.faire.detekt.rules

import com.faire.detekt.utils.getTypeName
import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtNullableType
import org.jetbrains.kotlin.psi.KtTypeElement
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtUserType

internal class NoPairWithAmbiguousTypes(config: Config = Config.empty) : Rule(config) {
  override val issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.Warning,
      description = "This rule prevents developers from using Pair<T, T> or Pair with an Any type parameter",
      debt = Debt.FIVE_MINS,
  )

  override fun visitNamedFunction(function: KtNamedFunction) {
    super.visitNamedFunction(function)

    val offendingParameters = function.valueParameters.filter { it.typeReference?.isOffendingType() == true }

    for (offendingParameter in offendingParameters) {
      report(
          CodeSmell(
              issue = issue,
              entity = Entity.from(function),
              message = "The function ${function.name} has parameter ${offendingParameter.name} which should be " +
                  "a class instead",
          ),
      )
    }

    if (function.typeReference?.isOffendingType() == true) {
      report(
          CodeSmell(
              issue = issue,
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
  val genericTypeNames = genericTypes.mapNotNull { generictype ->
    val typeName = generictype.getTypeName()
    val genericTypeNames = generictype.typeElement?.getGenericTypes()?.map { it.node.text }
    if (typeName == null || genericTypeNames == null) {
      null
    } else {
      "$typeName:$genericTypeNames".trimEnd('?')
    }
  }
  return (genericTypeNames.size == 2 && genericTypeNames[0].trimEnd('?') == genericTypeNames[1].trimEnd('?')) ||
      genericTypeNames.any { it == "Any:[]" }
}

private fun KtTypeElement.getGenericTypes(): List<KtTypeReference> {
  return when (this) {
    is KtUserType -> this.typeArgumentsAsTypes
    is KtNullableType -> this.typeArgumentsAsTypes
    else -> listOf()
  }.filterNotNull()
}
