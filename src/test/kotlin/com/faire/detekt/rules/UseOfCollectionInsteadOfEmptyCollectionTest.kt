package com.faire.detekt.rules

import com.faire.detekt.rules.UseOfCollectionInsteadOfEmptyCollection.Companion.ISSUE
import com.faire.detekt.utils.AutoCorrectRuleTest
import org.junit.jupiter.api.Test

internal class UseOfCollectionInsteadOfEmptyCollectionTest :
    AutoCorrectRuleTest<UseOfCollectionInsteadOfEmptyCollection>(
        { UseOfCollectionInsteadOfEmptyCollection(it) },
    ) {
  @Test
  fun `emptyList is caught`() {
    assertLintAndFormat(
        """
          fun `emptyList`() {
            val emptyList = emptyList()
          }
        """.trimIndent(),
        """
          fun `emptyList`() {
            val emptyList = listOf()
          }
        """.trimIndent(),
        issueDescription = ISSUE,
    )
  }

  @Test
  fun `emptyMap is caught`() {
    assertLintAndFormat(
        """
          fun `emptyMap`() {
            val emptyMap = emptyMap()
          }
        """.trimIndent(),
        """
          fun `emptyMap`() {
            val emptyMap = mapOf()
          }
        """.trimIndent(),
        issueDescription = ISSUE,
    )
  }

  @Test
  fun `emptySet is caught`() {
    assertLintAndFormat(
        """
          fun `emptySet`() {
            val emptySet = emptySet()
          }
        """.trimIndent(),
        """
          fun `emptySet`() {
            val emptySet = setOf()
          }
        """.trimIndent(),
        issueDescription = ISSUE,
    )
  }

  @Test
  fun `empty collection references are caught in property initializers`() {
    assertLintAndFormat(
        """
          class Something {
            val set = emptySet()
            val list = emptyList()
            val map = emptyMap()
          }
        """.trimIndent(),
        """
          class Something {
            val set = setOf()
            val list = listOf()
            val map = mapOf()
          }
        """.trimIndent(),
        issueDescription = ISSUE,
    )
  }

  @Test
  fun `empty collection references are caught in default arguments`() {
    assertLintAndFormat(
        """
          fun test(set: Set = emptySet(), map: Map = emptyMap(), list: List = emptyList()) {
            set.toList()
          }
        """.trimIndent(),
        """
          fun test(set: Set = setOf(), map: Map = mapOf(), list: List = listOf()) {
            set.toList()
          }
        """.trimIndent(),
        issueDescription = ISSUE,
    )
  }

  @Test
  fun `empty collection references are caught in function call`() {
    assertLintAndFormat(
        """
          fun takeASet(set: Set) : List {
            return set.toList()
          }
          
          fun takeAList(list: List) : Set {
            return list.toSet()
          }
          
          fun takeAMap(map: Map) : Set {
            return map.keys()
          }
          
          fun test() {
            takeASet(emptySet())
            takeAList(emptyList())
            takeAMap(emptyMap())
          }
        """.trimIndent(),
        """
          fun takeASet(set: Set) : List {
            return set.toList()
          }
          
          fun takeAList(list: List) : Set {
            return list.toSet()
          }
          
          fun takeAMap(map: Map) : Set {
            return map.keys()
          }
          
          fun test() {
            takeASet(setOf())
            takeAList(listOf())
            takeAMap(mapOf())
          }
        """.trimIndent(),
        issueDescription = ISSUE,
    )
  }
}
