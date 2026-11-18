package com.faire.detekt.rules

import com.faire.detekt.utils.AutoCorrectRuleTest
import org.junit.jupiter.api.Test

private const val ISSUE_DESCRIPTION =
    "use ignoreCase=true with various string matching functions without converting to lowercase"

internal class PreferIgnoreCaseTest :
    AutoCorrectRuleTest<PreferIgnoreCase>(
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

  @Test
  fun `auto correct keeps a trailing comma valid`() {
    assertLintAndFormat(
        """
          val str = "string".lowercase().endsWith(
              "ing",
          )
        """.trimIndent(),
        """
          val str = "string".endsWith(
              "ing", ignoreCase = true,
          )
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `auto correct char and lowercase() arguments`() {
    assertLintAndFormat(
        """
          fun find(name: String, query: String) {
            name.lowercase().indexOf('a')
            name.lowercase().contains(query.lowercase())
          }
        """.trimIndent(),
        """
          fun find(name: String, query: String) {
            name.indexOf('a', ignoreCase = true)
            name.contains(query.lowercase(), ignoreCase = true)
          }
        """.trimIndent(),
        issueDescription = ISSUE_DESCRIPTION,
    )
  }

  @Test
  fun `report but do not auto correct when ignoring case could change the result`() {
    assertLintedButNotFormatted(
        """
          fun find(name: String, query: String) {
            name.lowercase().contains(query)
            name.lowercase().startsWith("Foo")
            name.lowercase().endsWith("\u0046oo")
          }
        """.trimIndent(),
    )
  }

  @Test
  fun `report but do not auto correct a call that already passes ignoreCase`() {
    assertLintedButNotFormatted(
        """
          fun find(name: String) {
            name.lowercase().contains("foo", ignoreCase = true)
            name.lowercase().contains("foo", true)
          }
        """.trimIndent(),
    )
  }

  @Test
  fun `report but do not auto correct a Regex argument`() {
    assertLintedButNotFormatted(
        """
          val found = "string".lowercase().contains(Regex("st+r"))
        """.trimIndent(),
    )
  }
}
