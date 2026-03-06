package com.faire.detekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtFunctionType
import org.jetbrains.kotlin.psi.KtNullableType
import org.jetbrains.kotlin.psi.KtParameter

internal class NoNullableLambdaWithDefaultNull(config: Config = Config.empty) :
    Rule(config, "Instead of using nullable callbacks with default value of null, use non-nullable callbacks with a default empty lambda.") {
//  override val defaultRuleIdAliases = setOf("NO_NULLABLE_CALLBACK_WITH_DEFAULT_NULL")

  override fun visitParameter(parameter: KtParameter) {
    super.visitParameter(parameter)
    val defaultValue = parameter.defaultValue

    if (defaultValue?.text == "null" && parameter.typeReference?.typeElement is KtNullableType) {
      val type = parameter.typeReference?.typeElement?.let { it as? KtNullableType }?.innerType as? KtFunctionType
      val typeElement = type?.text

      // examples of typeElement: (String) -> Unit, (Int) -> Unit, (String, Int) -> Unit
      if (typeElement?.startsWith("(") == true && typeElement.endsWith(") -> Unit")) {
        report(
            Finding(
                entity = Entity.from(parameter),
                message = "Replace 'null' with an empty lambda expression '{}' " +
                    "for the default value of the function parameter.",
            ),
        )
      }
    }
  }
}
