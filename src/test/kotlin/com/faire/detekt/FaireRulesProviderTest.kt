package com.faire.detekt

import dev.detekt.api.Rule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.lang.reflect.Modifier

internal class FaireRulesProviderTest {
  private val registeredRuleNames = FaireRulesProvider().instance().rules.keys.map { it.value }

  @Test
  fun `every rule is registered`() {
    val ruleNames = File("src/main/kotlin/com/faire/detekt/rules").walk()
        .filter { file -> file.extension == "kt" }
        .map { file -> Class.forName("com.faire.detekt.rules.${file.nameWithoutExtension}") }
        .filter { ruleClass ->
            Rule::class.java.isAssignableFrom(ruleClass) && !Modifier.isAbstract(ruleClass.modifiers)
        }
        .map { ruleClass -> ruleClass.simpleName }
        .toList()

    assertThat(ruleNames).isNotEmpty()
    assertThat(registeredRuleNames).containsExactlyInAnyOrderElementsOf(ruleNames)
  }

  @Test
  fun `the README configuration lists every rule`() {
    val readmeRuleNames = Regex("""^ {2}(\w+):$""", RegexOption.MULTILINE)
        .findAll(File("README.md").readText())
        .map { match -> match.groupValues[1] }
        .toList()

    assertThat(readmeRuleNames).containsExactlyInAnyOrderElementsOf(registeredRuleNames)
  }
}
