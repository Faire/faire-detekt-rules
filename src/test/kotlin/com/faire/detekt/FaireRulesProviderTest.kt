package com.faire.detekt

import dev.detekt.api.Config
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.lang.reflect.Modifier

internal class FaireRulesProviderTest {
  private val rules = FaireRulesProvider().instance().rules
  private val registeredRuleNames = rules.keys.map { it.value }
  private val readme = File("README.md").readText()

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
    assertThat(registeredRuleNames).containsExactlyInAnyOrderElementsOf(ruleNames)
  }

  @Test
  fun `the README configuration lists every rule`() {
    val readmeRuleNames = Regex("""^ {2}(\w+):$""", RegexOption.MULTILINE)
        .findAll(readme)
        .map { match -> match.groupValues[1] }
        .toList()

    assertThat(readmeRuleNames).containsExactlyInAnyOrderElementsOf(registeredRuleNames)
  }

  @Test
  fun `the README lists the rules that need type resolution`() {
    val typeResolutionRuleNames = rules
        .filterValues { createRule -> createRule(Config.empty) is RequiresAnalysisApi }
        .keys
        .map { it.value }
    val readmeRuleNames = Regex("""`(\w+)`""")
        .findAll(readme.substringAfter("## Type Resolution").substringBefore("\n#"))
        .map { match -> match.groupValues[1] }
        .filter { name -> name in registeredRuleNames }
        .toList()

    assertThat(typeResolutionRuleNames).isNotEmpty()
    assertThat(readmeRuleNames).containsExactlyInAnyOrderElementsOf(typeResolutionRuleNames)
  }
}
