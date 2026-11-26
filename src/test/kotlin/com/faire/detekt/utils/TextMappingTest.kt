package com.faire.detekt.utils

import com.intellij.openapi.util.TextRange
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TextMappingTest {
  @Test
  fun `maps a range in identical texts to itself`() {
    assertThat(TextMapping("foo()", "foo()").map(TextRange(0, 5))).isEqualTo(TextRange(0, 5))
  }

  @Test
  fun `maps a range after inserted text`() {
    assertThat(TextMapping("foo()", "  foo()").map(TextRange(0, 5))).isEqualTo(TextRange(2, 7))
  }

  @Test
  fun `maps a range after removed text`() {
    assertThat(TextMapping("  foo()", "foo()").map(TextRange(2, 7))).isEqualTo(TextRange(0, 5))
  }

  @Test
  fun `maps a range right before inserted text`() {
    assertThat(TextMapping("foo()", "foo(),").map(TextRange(0, 5))).isEqualTo(TextRange(0, 5))
  }

  @Test
  fun `does not map a range that contains a change`() {
    assertThat(TextMapping("foo( )", "foo()").map(TextRange(0, 6))).isNull()
  }

  @Test
  fun `maps nothing when the texts differ in more characters than allowed`() {
    assertThat(TextMapping("foo()", "  foo()", maxEdits = 2).map(TextRange(0, 5))).isEqualTo(TextRange(2, 7))
    assertThat(TextMapping("foo()", "  foo()", maxEdits = 1).map(TextRange(0, 5))).isNull()
  }
}
