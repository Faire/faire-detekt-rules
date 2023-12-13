package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRuleTest
import org.junit.jupiter.api.Test

private const val ISSUE_DESCRIPTION =
    "use ignoreCase=true with various string matching functions without converting to lowercase"

internal class PreferIgnoreCaseTest : AutoCorrectRuleTest<PreferIgnoreCase>(
    { PreferIgnoreCase(it) },
) {
  @Test
  fun `auto correct lowercase+contains`() {
    assertLintAndFormat(
        """
          val str = "string".lowercase().contains("str")
        """.trimIndent(),
        """
          val str = "string".contains("str", ignoreCase = true)
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `auto correct toLowerCase+startsWith`() {
    assertLintAndFormat(
        """
          val str = "string".toLowerCase().startsWith("str")
        """.trimIndent(),
        """
          val str = "string".startsWith("str", ignoreCase = true)
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }
}
