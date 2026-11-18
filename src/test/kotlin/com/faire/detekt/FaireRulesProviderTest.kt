package com.faire.detekt

import dev.detekt.api.Rule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.lang.reflect.Modifier

internal class FaireRulesProviderTest {
  @Test
  fun `every rule is registered`() {
    val ruleNames = File("src/main/kotlin/com/faire/detekt/rules")
        .listFiles { file -> file.extension == "kt" }
        .orEmpty()
        .mapNotNull { file ->
            val ruleClass = Class.forName("com.faire.detekt.rules.${file.nameWithoutExtension}")
            val isRule = Rule::class.java.isAssignableFrom(ruleClass) && !Modifier.isAbstract(ruleClass.modifiers)
            ruleClass.simpleName.takeIf { isRule }
        }

    assertThat(ruleNames).isNotEmpty()
    assertThat(FaireRulesProvider().instance().rules.keys.map { it.value })
        .containsExactlyInAnyOrderElementsOf(ruleNames)
  }
}
