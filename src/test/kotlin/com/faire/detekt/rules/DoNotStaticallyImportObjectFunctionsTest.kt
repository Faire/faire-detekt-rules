package com.faire.detekt.rules

import dev.detekt.test.TestConfig
import dev.detekt.test.junit.KotlinCoreEnvironmentTest
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.KotlinEnvironmentContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val DEPENDENCIES =
    """
    package com.faire.lib

    fun topLevelFunction(): Int = 1

    object MySpecificObject {
      val someProperty: Int = 1

      fun theStaticFunction(): Int = 1

      fun String.someExtension(): Int = 1
    }

    object Assertions {
      fun assertThat(value: Any): Any = value
    }

    class ApiException {
      companion object {
        fun badRequest(): ApiException = ApiException()
      }

      object Factory {
        fun notFound(): ApiException = ApiException()
      }
    }

    class WithCompanion {
      companion object {
        fun create(): WithCompanion = WithCompanion()
      }
    }

    class Outer {
      class Nested
    }

    enum class SomeEnum { FIRST }
    """

@KotlinCoreEnvironmentTest
internal class DoNotStaticallyImportObjectFunctionsTest(private val env: KotlinEnvironmentContainer) {
  private val rule = DoNotStaticallyImportObjectFunctions()

  @Test
  fun `statically importing an object function is reported`() {
    val findings = rule.lintWithContext(
        env,
        """
        import com.faire.lib.MySpecificObject.theStaticFunction

        fun usage(): Int = theStaticFunction()
        """.trimIndent(),
        DEPENDENCIES,
    )

    assertThat(findings).singleElement().satisfies({
      assertThat(it.message).isEqualTo(
          "Do not statically import functions of MySpecificObject, " +
              "call them as MySpecificObject.theStaticFunction() instead.",
      )
    })
  }

  @Test
  fun `aliased static import of an object function is reported`() {
    val findings = rule.lintWithContext(
        env,
        """
        import com.faire.lib.MySpecificObject.theStaticFunction as aliased

        fun usage(): Int = aliased()
        """.trimIndent(),
        DEPENDENCIES,
    )

    assertThat(findings).hasSize(1)
  }

  @Test
  fun `statically importing a companion object function is reported against the containing class`() {
    val findings = rule.lintWithContext(
        env,
        """
        import com.faire.lib.WithCompanion.Companion.create

        fun usage() = create()
        """.trimIndent(),
        DEPENDENCIES,
    )

    assertThat(findings).singleElement().satisfies({
      assertThat(it.message).isEqualTo(
          "Do not statically import functions of WithCompanion, call them as WithCompanion.create() instead.",
      )
    })
  }

  @Test
  fun `statically importing a java static method is reported`() {
    val findings = rule.lintWithContext(
        env,
        """
        import java.util.Collections.emptyList

        fun usage(): List<Int> = emptyList()
        """.trimIndent(),
    )

    assertThat(findings).hasSize(1)
  }

  @Test
  fun `star importing java static methods is reported`() {
    val findings = rule.lintWithContext(
        env,
        """
        import java.util.Collections.*

        fun usage(): List<Int> = emptyList()
        """.trimIndent(),
    )

    assertThat(findings).hasSize(1)
  }

  @Test
  fun `referencing an object function through its type is not reported`() {
    val findings = rule.lintWithContext(
        env,
        """
        import com.faire.lib.MySpecificObject
        import com.faire.lib.WithCompanion

        fun usage(): Int {
          WithCompanion.create()
          return MySpecificObject.theStaticFunction()
        }
        """.trimIndent(),
        DEPENDENCIES,
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `importing non function members and types is not reported`() {
    val findings = rule.lintWithContext(
        env,
        """
        import com.faire.lib.MySpecificObject.someExtension
        import com.faire.lib.MySpecificObject.someProperty
        import com.faire.lib.Outer.Nested
        import com.faire.lib.SomeEnum.FIRST
        import com.faire.lib.topLevelFunction

        fun usage(): List<Any> = listOf("a".someExtension(), someProperty, Nested(), FIRST, topLevelFunction())
        """.trimIndent(),
        DEPENDENCIES,
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `functions of allowed types and their nested objects are not reported`() {
    val configuredRule = DoNotStaticallyImportObjectFunctions(
        TestConfig("allowedTypes" to listOf("Assertions", "ApiException")),
    )

    val findings = configuredRule.lintWithContext(
        env,
        """
        import com.faire.lib.ApiException.Companion.badRequest
        import com.faire.lib.ApiException.Factory.notFound
        import com.faire.lib.Assertions.assertThat

        fun usage(): Any = assertThat(badRequest() to notFound())
        """.trimIndent(),
        DEPENDENCIES,
    )

    assertThat(findings).isEmpty()
  }

  @Test
  fun `allowed types can be configured by simple or fully qualified name`() {
    val configuredRule = DoNotStaticallyImportObjectFunctions(
        TestConfig("allowedTypes" to listOf("com.faire.lib.MySpecificObject", "WithCompanion")),
    )

    val findings = configuredRule.lintWithContext(
        env,
        """
        import com.faire.lib.Assertions.assertThat
        import com.faire.lib.MySpecificObject.theStaticFunction
        import com.faire.lib.WithCompanion.Companion.create

        fun usage(): Any = assertThat(theStaticFunction() to create())
        """.trimIndent(),
        DEPENDENCIES,
    )

    assertThat(findings).singleElement().satisfies({
      assertThat(it.message).contains("Assertions.assertThat()")
    })
  }
}
