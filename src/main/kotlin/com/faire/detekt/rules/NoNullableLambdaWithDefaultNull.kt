package com.faire.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtFunctionType
import org.jetbrains.kotlin.psi.KtNullableType
import org.jetbrains.kotlin.psi.KtParameter

internal class NoNullableLambdaWithDefaultNull(config: Config = Config.empty) : Rule(config) {
  override val defaultRuleIdAliases = setOf("NO_NULLABLE_CALLBACK_WITH_DEFAULT_NULL")

  override val issue = Issue(
      id = javaClass.simpleName,
      severity = Severity.Style,
      debt = Debt.FIVE_MINS,
      description = "Instead of using nullable callbacks with default value of null, " +
          "use non-nullable callbacks with a default empty lambda.",
  )

  override fun visitParameter(parameter: KtParameter) {
    super.visitParameter(parameter)
    val defaultValue = parameter.defaultValue

    if (defaultValue?.text == "null" && parameter.typeReference?.typeElement is KtNullableType) {
      val type = parameter.typeReference?.typeElement?.let { it as? KtNullableType }?.innerType as? KtFunctionType
      val typeElement = type?.text

      // examples of typeElement: (String) -> Unit, (Int) -> Unit, (String, Int) -> Unit
      if (typeElement?.startsWith("(") == true && typeElement.endsWith(") -> Unit")) {
        report(
            CodeSmell(
                issue = issue,
                entity = Entity.from(parameter),
                message = "Replace 'null' with an empty lambda expression '{}' " +
                    "for the default value of the function parameter.",
            ),
        )
      }
    }
  }
}
