package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRuleTest
import org.junit.jupiter.api.Test

internal class NoNonPrivateGlobalVariablesTest :
    AutoCorrectRuleTest<NoNonPrivateGlobalVariables>(
    { NoNonPrivateGlobalVariables(it) },
) {
  @Test
  fun `should not detekt variables in classes`() {
    assertNoViolations(
        """
          package com.faire.detekt.rules

          class Foo {
            private val bar = 1
            val cool = 2
            internal val notCool = 3
            const val constCool = 4
          }
        """.trimIndent(),
    )
  }

  @Test
  fun `should not detekt variables in objects`() {
    assertNoViolations(
        """
          package com.faire.detekt.rules

          object Foo {
            private val bar = 1
            val cool = 2
            internal val notCool = 3
            const val constCool = 4
          }
        """.trimIndent(),
    )
  }

  @Test
  fun `should not detekt private global variables`() {
    assertNoViolations(
        """
          private val bar = 1
          private const val constBar = 2
          private var baz = 3
        """.trimIndent(),
    )
  }

  @Test
  fun `should not detekt extension values`() {
    assertNoViolations(
        """
          package com.faire.detekt.rules

          class Boo(private val secret: String)

          val Boo.hash: String
            get() = secret.hashCode().toString()

          val String.isFoo: Boolean
            get() = this == "foo"
        """.trimIndent(),
    )
  }

  @Test
  fun `should detekt and auto correct internal global variables`() {
    assertLintAndFormat(
        """
          package com.faire.detekt.rules

          internal val bar = 1
          internal const val constBar = 2
          internal var baz = 3
        """.trimIndent(),
        """
          package com.faire.detekt.rules

          private val bar = 1
          private const val constBar = 2
          private var baz = 3
        """.trimIndent(),
        issueDescription = NoNonPrivateGlobalVariables.RULE_DESCRIPTION,
    )
  }

  @Test
  fun `should detekt and auto correct public global variables`() {
    assertLintAndFormat(
        """
          package com.faire.detekt.rules
          
          val bar = 1
          const val constBar = 2
          var baz = 3
        """.trimIndent(),
        """
          package com.faire.detekt.rules
          
          private val bar = 1
          private const val constBar = 2
          private var baz = 3
        """.trimIndent(),
        issueDescription = NoNonPrivateGlobalVariables.RULE_DESCRIPTION,
    )
    assertLintAndFormat(
        """
          package com.faire.detekt.rules

          public val bar = 1
          public const val constBar = 2
          public var baz = 3
        """.trimIndent(),
        """
          package com.faire.detekt.rules
          
          private val bar = 1
          private const val constBar = 2
          private var baz = 3
        """.trimIndent(),
        issueDescription = NoNonPrivateGlobalVariables.RULE_DESCRIPTION,
    )
  }

  @Test
  fun `should only replace the visibility modifier, not documentation that mentions it`() {
    assertLintAndFormat(
        """
          package com.faire.detekt.rules

          /** Cache size used internally by the loader. */
          internal val cacheSize = 10

          // Base URL of the public API.
          public val baseUrl = "https://example.com"
        """.trimIndent(),
        """
          package com.faire.detekt.rules

          /** Cache size used internally by the loader. */
          private val cacheSize = 10

          // Base URL of the public API.
          private val baseUrl = "https://example.com"
        """.trimIndent(),
        issueDescription = NoNonPrivateGlobalVariables.RULE_DESCRIPTION,
    )
  }

  @Test
  fun `should add private after the KDoc and annotations`() {
    assertLintAndFormat(
        """
          package com.faire.detekt.rules

          /** Default request timeout in seconds. */
          @Suppress("MagicNumber")
          const val TIMEOUT_SECONDS = 30
        """.trimIndent(),
        """
          package com.faire.detekt.rules

          /** Default request timeout in seconds. */
          @Suppress("MagicNumber")
          private const val TIMEOUT_SECONDS = 30
        """.trimIndent(),
        issueDescription = NoNonPrivateGlobalVariables.RULE_DESCRIPTION,
    )
  }
}
