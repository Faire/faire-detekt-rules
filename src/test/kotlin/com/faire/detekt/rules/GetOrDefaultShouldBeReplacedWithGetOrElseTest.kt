package com.faire.detekt.rules

import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GetOrDefaultShouldBeReplacedWithGetOrElseTest {
  private lateinit var rule: GetOrDefaultShouldBeReplacedWithGetOrElse

  @BeforeEach
  fun setup() {
    rule = GetOrDefaultShouldBeReplacedWithGetOrElse()
  }

  @Test
  fun `usage of getOrDefault is flagged`() {
    val findings = rule.lint(
        """
          fun usage() {
            val map = mapOf(
              "Dan" to 3,
              "Bob" to 7
            )
            
            if(map.getOrDefault("Dan", 0) > 3) {
              println("Gotcha!")
            }
          }
        """.trimIndent(),
    )

    assertThat(findings).hasSize(1)
  }

  @Test
  fun `usage of custom top level getOrDefault function is not flagged`() {
    val findings = rule.lint(
        """
          fun usage() {
            getOrDefault()
            getOrDefault(true)
            getOrDefault(true, 3)
          }
          
          fun getOrDefault(): Int = 3
          
          fun getOrDefault(a: Boolean): Boolean = a
          
          fun getOrDefault(a: Boolean, b: Int): Boolean = a && b > 3
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `usage of custom getOrDefault member function in class is not flagged`() {
    val findings = rule.lint(
        """
          class Person(val name: String) {
            fun usage() {
              getOrDefault()
              getOrDefault(true)
              getOrDefault(true, 3)
            }
                        
            fun getOrDefault(): Int = 3
                      
            fun getOrDefault(a: Boolean): Boolean = a
                      
            fun getOrDefault(a: Boolean, b: Int): Boolean = a && b > 3
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `usage of custom getOrDefault member function outside of class is not flagged`() {
    // TODO(Dan): Uncomment the usage of the member function with 2 parameters after the rule is enhanced
    val findings = rule.lint(
        """
          class Person(val name: String) {
            fun getOrDefault(): Int = 3
                      
            fun getOrDefault(a: Boolean): Boolean = a
                      
            fun getOrDefault(a: Boolean, b: Int): Boolean = a && b > 3
          }
          
          fun usage() {
            val person = Person("Dan")
            
            person.getOrDefault()
            person.getOrDefault(true)
            //person.getOrDefault(true, 3)
          }
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `usage of custom getOrDefault extension function is not flagged`() {
    val findings = rule.lint(
        """
          fun usage() {
            3.getOrDefault()
            "hello".getOrDefault(true)
            Integer.valueOf(500).getOrDefault(true, 3)
          }
          
          fun Any.getOrDefault(): Int = 3
          
          fun String.getOrDefault(a: Boolean): Boolean = a
          
          fun Any?.getOrDefault(a: Boolean, b: Int): Boolean = a && b > 3
        """.trimIndent(),
    )

    assertThat(findings).isEmpty()
  }
}
